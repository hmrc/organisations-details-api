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

package uk.gov.hmrc.organisationsdetailsapi.controllers

import play.api.libs.json._
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import play.api.{Logger, Logging}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthorisationException, AuthorisedFunctions, Enrolment, InsufficientEnrolments}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, TooManyRequestException, UpstreamErrorResponse}
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.errorhandler.ErrorResponses._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseApiController(cc: ControllerComponents)
    extends BackendController(cc) with AuthorisedFunctions with PrivilegedAuthentication {

  protected override val logger: Logger = play.api.Logger(this.getClass)

  protected override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(rh)

  def withValidJson[T](f: T => Future[Result])(implicit request: Request[JsValue], r: Reads[T]): Future[Result] =
    request.body.validate[T] match {
      case JsSuccess(t, _) => f(t)
      case JsError(_) =>
        Future.failed(new BadRequestException("Malformed payload"))
    }

  def recoveryWithAudit(correlationId: Option[String], matchId: String, url: String)(implicit
    request: RequestHeader,
    auditHelper: AuditHelper
  ): PartialFunction[Throwable, Result] = {
    case UpstreamErrorResponse(_, 404, _, _) =>
      logger.warn("Controllers UpstreamErrorResponse encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, "UpstreamErrorResponse")
      ErrorNotFound.toHttpResponse
    case e: InsufficientEnrolments =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized("Insufficient Enrolments").toHttpResponse
    case e: AuthorisationException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorUnauthorized(e.getMessage).toHttpResponse
    case tmr: TooManyRequestException =>
      logger.warn("Controllers TooManyRequestException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, tmr.getMessage)
      ErrorTooManyRequests.toHttpResponse
    case br: BadRequestException =>
      auditHelper.auditApiFailure(correlationId, matchId, request, url, br.getMessage)
      ErrorInvalidRequest(br.getMessage).toHttpResponse
    case e: IllegalArgumentException =>
      logger.warn("Controllers IllegalArgumentException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case e: NotFoundException =>
      logger.warn("Controllers NotFoundException encountered")
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorNotFound.toHttpResponse
    case e: Exception =>
      logger.error("Unexpected exception", e)
      auditHelper.auditApiFailure(correlationId, matchId, request, url, e.getMessage)
      ErrorInternalServer("Something went wrong.").toHttpResponse
  }
}

case class SchemaValidationError(keyword: String, msgs: Seq[String], instancePath: String)

object SchemaValidationError {
  implicit val format: OFormat[SchemaValidationError] = Json.format
}

trait PrivilegedAuthentication extends AuthorisedFunctions with Logging {

  def authPredicate(scopes: Iterable[String]): Predicate =
    scopes.map(Enrolment(_): Predicate).reduce(_ or _)

  def authenticate(endpointScopes: Iterable[String], matchId: String)(f: Iterable[String] => Future[Result])(implicit
    hc: HeaderCarrier,
    request: RequestHeader,
    auditHelper: AuditHelper,
    ec: ExecutionContext
  ): Future[Result] =
    if endpointScopes.isEmpty then throw new Exception("No scopes defined")
    else {
      val predicate = authPredicate(endpointScopes)
      authorised(predicate)
        .retrieve(Retrievals.allEnrolments) { scopes =>
          auditHelper.auditAuthScopes(matchId, scopes.enrolments.map(e => e.key).mkString(","), request)

          f(scopes.enrolments.map(e => e.key))
        }
        .recoverWith {
          case e: InsufficientEnrolments =>
            logger.error(s"$matchId - insufficient enrollments", e)
            Future.failed(e)
          case e: AuthorisationException =>
            logger.error(s"$matchId - authorisation exception", e)
            Future.failed(e)
        }
    }
}
