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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.play.RequestHeaderUtils.{maybeCorrelationId, validateCorrelationId}
import uk.gov.hmrc.organisationsdetailsapi.services.{ScopesService, VatReturnDetailsService}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import play.api.hal.Hal.state
import play.api.hal.HalLink
import uk.gov.hmrc.http.BadRequestException

class VatReturnDetailsController @Inject()(val authConnector: AuthConnector,
                                           cc: ControllerComponents,
                                           vatService: VatReturnDetailsService,
                                           implicit val auditHelper: AuditHelper,
                                           scopesService: ScopesService)
                                          (implicit ec: ExecutionContext) extends BaseApiController(cc) {
  def vat(matchId: UUID, appDate: String): Action[AnyContent] = Action.async { implicit request =>
    authenticate(scopesService.getEndPointScopes("vat"), matchId.toString) { authScopes =>
      val correlationId = validateCorrelationId(request)
      validateAppDate(appDate)
      vatService.get(matchId, appDate, authScopes).map { vatResponse =>
        val selfLink = HalLink("self", s"/organisations/details/vat?matchId=$matchId&appDate=$appDate")

        val response = Json.toJson(state(vatResponse) ++ selfLink)

        auditHelper.auditApiResponse(
          correlationId.toString,
          matchId.toString,
          authScopes.mkString(","),
          request,
          selfLink.toString,
          Some(Json.toJson(response))
        )

        Ok(response)
      }
    } recover recoveryWithAudit(maybeCorrelationId(request), matchId.toString, "/organisations/details/vat")
  }

  private def validateAppDate(appDate: String): Unit =
    if (!appDate.matches("^[0-9]{8}$"))
      throw new BadRequestException("AppDate is incorrect")
}
