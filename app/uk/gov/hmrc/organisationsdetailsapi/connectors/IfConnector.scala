/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.organisationsdetailsapi.connectors

import play.api.Logger
import play.api.libs.json.Writes
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountRequest._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountResponse._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.SelfAssessmentReturnDetail._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.IfVatReturnDetailsResponse._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{CorporationTaxReturnDetailsResponse, EmployeeCountRequest, EmployeeCountResponse, SelfAssessmentReturnDetailResponse, IfVatReturnDetailsResponse}
import uk.gov.hmrc.organisationsdetailsapi.play.RequestHeaderUtils.validateCorrelationId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IfConnector @Inject()(
                             servicesConfig: ServicesConfig,
                             http: HttpClient,
                             val auditHelper: AuditHelper,
                           ) {

  private val logger = Logger(classOf[IfConnector].getName)
  private val baseUrl = servicesConfig.baseUrl("integration-framework")

  private val integrationFrameworkBearerToken =
    servicesConfig.getString(
      "microservice.services.integration-framework.authorization-token"
    )

  private val integrationFrameworkEnvironment = servicesConfig.getString(
    "microservice.services.integration-framework.environment"
  )

  def getCtReturnDetails(matchId: String, utr: String, filter: Option[String])(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[CorporationTaxReturnDetailsResponse] = {

    val corporationTaxUrl =
      s"$baseUrl/organisations/corporation-tax/$utr/return/details${
        filter.map(f => s"?fields=$f").getOrElse("")
      }"

    call[CorporationTaxReturnDetailsResponse](corporationTaxUrl, matchId)
  }

  def getVatReturnPeriods(matchId: String, vrn: String, appDate: String, filter: Option[String])(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[IfVatReturnDetailsResponse] = {

    val vatTaxUrl =
      s"$baseUrl/organisations/vat/$vrn/returns-details?appDate=$appDate${
        filter.map(f => s"&fields=$f").getOrElse("")
      }"

    call[IfVatReturnDetailsResponse](vatTaxUrl, matchId)
  }

  def getSaReturnDetails(matchId: String, utr: String, filter: Option[String])(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[SelfAssessmentReturnDetailResponse] = {

    val detailsUrl =
      s"$baseUrl/organisations/self-assessment/$utr/return/details${
        filter.map(f => s"?fields=$f").getOrElse("")
      }"

    call[SelfAssessmentReturnDetailResponse](detailsUrl, matchId)
  }

  def getEmployeeCount(matchId: String, utr: String, body: EmployeeCountRequest, filter: Option[String])(
    implicit hc: HeaderCarrier,
    request: RequestHeader,
    ec: ExecutionContext): Future[EmployeeCountResponse] = {

    val detailsUrl =
      s"$baseUrl/organisations/employers/employee/counts${
        filter.map(f => s"?fields=$f").getOrElse("")
      }"

    post[EmployeeCountRequest, EmployeeCountResponse](detailsUrl, matchId, body)
  }

  private def extractCorrelationId(requestHeader: RequestHeader) = validateCorrelationId(requestHeader).toString

  def setHeaders(requestHeader: RequestHeader): Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> s"Bearer $integrationFrameworkBearerToken",
    "Environment" -> integrationFrameworkEnvironment,
    "CorrelationId" -> extractCorrelationId(requestHeader)
  )

  private def call[T](url: String, matchId: String)
                     (implicit rds: HttpReads[T], hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover(http.GET[T](url, headers = setHeaders(request)) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, response.toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def post[I, O](url: String, matchId: String, body: I)
                        (implicit wts: Writes[I], reads: HttpReads[O], hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover(http.POST[I, O](url, body, headers = setHeaders(request)) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, response.toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def recover[A](x: Future[A],
                         correlationId: String,
                         matchId: String,
                         request: RequestHeader,
                         requestUrl: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = x.recoverWith {
    case validationError: JsValidationException =>
      logger.warn("Integration Framework JsValidationException encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl,
        s"Error parsing IF response: ${validationError.errors}")
      Future.failed(new InternalServerException("Something went wrong."))

    case Upstream5xxResponse(msg, code, _, _) =>
      logger.warn(s"Integration Framework Upstream5xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: $msg")
      Future.failed(new InternalServerException("Something went wrong."))

//    case UpstreamErrorResponse((msg, 400, _, _)) if requestUrl.contains("/vat") && msg.contains("INVALID_DATE") =>
//      logger.warn(s"Integration Framework returned invalid appDate error")
//      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Invalid appDate: $msg")
//      val invalidAppDate = request.getQueryString("appDate").mkString
//      Future.failed(new BadRequestException(s"Invalid appDate: $invalidAppDate"))

    case Upstream4xxResponse(msg, 429, _, _) =>
      logger.warn(s"IF Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"IF Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))

    case Upstream4xxResponse(msg, 404, _, _) =>
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      if (msg.contains("NO_DATA_FOUND") || msg.contains("NO_VAT_RETURNS_DETAIL_FOUND")) {
        noDataFound(requestUrl)
      } else {
        logger.warn(s"Integration Framework Upstream4xxResponse encountered: 404")
        Future.failed(new InternalServerException("Something went wrong."))
      }

    case Upstream4xxResponse(msg, code, _, _) =>
      logger.warn(s"Integration Framework Upstream4xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))

    case e: Exception =>
      logger.error(s"Integration Framework Exception encountered", e)
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
  }

  private def noDataFound[A](url: String): Future[A] = {
    lazy val emptyEmployeeCountResponse = EmployeeCountResponse(None, None, Some(Seq()))
    lazy val emptyCtReturn = CorporationTaxReturnDetailsResponse(None, None, None, Some(Seq()))
    lazy val emptySaReturn = SelfAssessmentReturnDetailResponse(None, None, None, None, Some(Seq()))

    if (url.contains("counts"))
      Future.successful(emptyEmployeeCountResponse.asInstanceOf[A])
    else if (url.contains("corporation-tax"))
      Future.successful(emptyCtReturn.asInstanceOf[A])
    else if (url.contains("self-assessment"))
      Future.successful(emptySaReturn.asInstanceOf[A])
    else if (url.contains("vat"))
      Future.failed(new NotFoundException("VAT details could not be found"))
    else
      Future.failed(new InternalServerException("Something went wrong."))
  }
}
