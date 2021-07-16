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

package unit.uk.gov.hmrc.organisationsdetailsapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.controllers.{LiveCorporationTaxController, SandboxCorporationTaxController}
import uk.gov.hmrc.organisationsdetailsapi.sandbox.CorporationTaxSandboxData
import uk.gov.hmrc.organisationsdetailsapi.services.{LiveCorporationTaxService, SandboxCorporationTaxService, ScopesService}
import utils.TestSupport
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.when
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CorporationTaxControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with TestSupport {

  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val sampleCorrelationIdHeader = ("CorrelationId" -> sampleCorrelationId)

  private val fakeRequest = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)

  implicit val sys = ActorSystem("MyTest")
  implicit val mat = ActorMaterializer()

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]

  private val mockLiveCorporationTaxService = mock[LiveCorporationTaxService]
  private val sandboxCorporationTaxService = new SandboxCorporationTaxService()

  private val liveController = new LiveCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), mockLiveCorporationTaxService, mockAuditHelper, mockScopesService)
  private val sandboxController = new SandboxCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), sandboxCorporationTaxService, mockAuditHelper, mockScopesService)

  "SandboxCorporationTaxController" should {
    "not need an bearer token to operate" in {

      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      val result = await(sandboxController.corporationTax(CorporationTaxSandboxData.sandboxMatchIdUUID)(fakeRequest))
      jsonBodyOf(result) shouldBe
        Json.parse(
          """
            |{
            |    "_links": {
            |        "self": {
            |            "href": "/organisations/details/corporation-tax?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
            |        }
            |    },
            |    "dateOfRegistration": "2015-04-21",
            |    "taxSolvencyStatus": "V",
            |    "periods": [
            |        {
            |            "accountingPeriodStartDate": "2018-04-06",
            |            "accountingPeriodEndDate": "2018-10-05",
            |            "turnover": 38390
            |        },
            |        {
            |            "accountingPeriodStartDate": "2018-10-06",
            |            "accountingPeriodEndDate": "2019-04-05",
            |            "turnover": 2340
            |        }
            |    ]
            |}
            |""".stripMargin)
    }

  }

}