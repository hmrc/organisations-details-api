/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.play.RequestHeaderUtils.validateCorrelationId
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Writes
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{CorporationTaxReturnDetailsResponse, EmployeeCountRequest, EmployeeCountResponse, SelfAssessmentReturnDetailResponse}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.SelfAssessmentReturnDetail._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountRequest._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountResponse._
import uk.gov.hmrc.organisationsdetailsapi.errorhandler.ErrorResponses.DataNotFoundException

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

  def setHeaders(requestHeader: RequestHeader) = Seq(
    HeaderNames.authorisation -> s"Bearer $integrationFrameworkBearerToken",
    "Environment"             -> integrationFrameworkEnvironment,
    "CorrelationId"           -> extractCorrelationId(requestHeader)
  )

  private def call[T](url: String, matchId: String)
                  (implicit rds: HttpReads[T], hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover(http.GET[T](url, headers = setHeaders(request)) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, response.toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def post[I,O](url: String, matchId: String, body: I)
                     (implicit wts: Writes[I], reads: HttpReads[O], hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext) =
    recover(http.POST[I,O](url, body, headers = setHeaders(request)) map { response =>
      auditHelper.auditIfApiResponse(extractCorrelationId(request), matchId, request, url, response.toString)
      response
    }, extractCorrelationId(request), matchId, request, url)

  private def recover[A](x: Future[A],
                         correlationId: String,
                         matchId: String,
                         request: RequestHeader,
                         requestUrl: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = x.recoverWith {
    case validationError: JsValidationException => {
      logger.warn("Integration Framework JsValidationException encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl,
        s"Error parsing IF response: ${validationError.errors}")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case Upstream5xxResponse(msg, code, _, _) => {
      logger.warn(s"Integration Framework Upstream5xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"Internal Server error: $msg")
      Future.failed(new InternalServerException("Something went wrong."))
    }
    case Upstream4xxResponse(msg, 429, _, _) => {
      logger.warn(s"IF Rate limited: $msg")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, s"IF Rate limited: $msg")
      Future.failed(new TooManyRequestException(msg))
    }
    case Upstream4xxResponse(msg, 404, _, _) => {
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      msg.contains("NO_DATA_FOUND") match {
        case true =>
          Future.failed(new DataNotFoundException(msg))
        case _ =>
          logger.warn(s"Integration Framework Upstream4xxResponse encountered: 404")
          Future.failed(new InternalServerException("Something went wrong."))
      }
    }
    case Upstream4xxResponse(msg, code, _, _) => {
      logger.warn(s"Integration Framework Upstream4xxResponse encountered: $code")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, msg)
      Future.failed(new InternalServerException("Something went wrong."))
    }

    case e: Exception => {
      logger.warn(s"Integration Framework Exception encountered")
      auditHelper.auditIfApiFailure(correlationId, matchId, request, requestUrl, e.getMessage)
      Future.failed(new InternalServerException("Something went wrong."))
    }
  }
}
