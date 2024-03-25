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

import play.api.Logger
import play.api.hal.Hal.state
import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.play.RequestHeaderUtils._
import uk.gov.hmrc.organisationsdetailsapi.services.{ScopesService, SelfAssessmentService}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SelfAssessmentController @Inject() (
  val authConnector: AuthConnector,
  cc: ControllerComponents,
  selfAssessmentService: SelfAssessmentService,
  implicit val auditHelper: AuditHelper,
  scopesService: ScopesService
)(implicit ec: ExecutionContext)
    extends BaseApiController(cc) with PrivilegedAuthentication {

  override val logger: Logger = Logger(classOf[SelfAssessmentController].getName)

  def selfAssessment(matchId: UUID): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopesService.getEndPointScopes("self-assessment"), matchId.toString) { authScopes =>
      val correlationId = validateCorrelationId(request)

      selfAssessmentService.get(matchId, "self-assessment", authScopes).map { selfAssessment =>
        val selfLink = HalLink("self", s"/organisations/details/self-assessment?matchId=$matchId")

        val response = Json.toJson(state(selfAssessment) ++ selfLink)

        auditHelper.auditApiResponse(
          correlationId.toString,
          matchId.toString,
          authScopes.mkString(","),
          request,
          selfLink.toString,
          Some(Json.toJson(selfAssessment))
        )

        Ok(response)
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), matchId.toString, "/organisations/details/self-assessment")
  }
}
