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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, PlayBodyParsers}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeesRequest
import uk.gov.hmrc.organisationsdetailsapi.play.RequestHeaderUtils.{maybeCorrelationId, validateCorrelationId}
import uk.gov.hmrc.organisationsdetailsapi.services.{NumberOfEmployeesService, ScopesService}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NumberOfEmployeesController @Inject()(val authConnector: AuthConnector,
                                            cc: ControllerComponents,
                                            numberOfEmployeesService: NumberOfEmployeesService,
                                            implicit val auditHelper: AuditHelper,
                                            scopesService: ScopesService,
                                            bodyParsers: PlayBodyParsers)
                                           (implicit ec: ExecutionContext) extends BaseApiController(cc) with PrivilegedAuthentication  {

  override val logger: Logger = Logger(classOf[NumberOfEmployeesController].getName)

  private val self = "/organisations/details/number-of-employees"

  def numberOfEmployees(matchId: UUID): Action[JsValue] = Action.async(bodyParsers.json) {
    implicit request =>
      authenticate(scopesService.getEndPointScopes("number-of-employees"), matchId.toString) { authScopes =>
        withValidJson[NumberOfEmployeesRequest] { employeeCountRequest =>
          val correlationId = validateCorrelationId(request)

          numberOfEmployeesService.get(matchId, employeeCountRequest, authScopes).map { numberOfEmployees =>
            val selfLink = HalLink("self", s"$self?matchId=$matchId")

            val arrayToReturn = numberOfEmployees.getOrElse(Seq.empty)

            val response = Json.toJson(state(Json.obj("employeeCounts" -> arrayToReturn)) ++ selfLink)

            auditHelper.auditNumberOfEmployeesApiResponse(correlationId.toString, matchId.toString,
              authScopes.mkString(","), request, selfLink.toString, Some(Json.toJson(numberOfEmployees)))

            Ok(response)
          }
        }
      } recover recoveryWithAudit(maybeCorrelationId(request), matchId.toString, self)
  }

}
