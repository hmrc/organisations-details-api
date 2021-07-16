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

import akka.stream.Materializer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.controllers.{LiveCorporationTaxController, SandboxCorporationTaxController}
import uk.gov.hmrc.organisationsdetailsapi.sandbox.CorporationTaxSandboxData
import uk.gov.hmrc.organisationsdetailsapi.services.{LiveCorporationTaxService, SandboxCorporationTaxService, ScopesService}
import utils.TestSupport

import scala.concurrent.ExecutionContext

class CorporationTaxControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar with TestSupport {

  implicit lazy val materializer: Materializer = fakeApplication.materializer
  implicit lazy val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]

  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val sampleCorrelationIdHeader = ("CorrelationId" -> sampleCorrelationId)

  private val fakeRequest = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)

  private val env           = Environment.simple()
  private val configuration = Configuration.load(env)

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]

  private val mockLiveCorporationTaxService = mock[LiveCorporationTaxService]
  private val mockSandboxCorporationTaxService = mock[SandboxCorporationTaxService]

  private val liveController = new LiveCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), mockLiveCorporationTaxService, mockAuditHelper, mockScopesService)
  private val sandboxController = new SandboxCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), mockSandboxCorporationTaxService, mockAuditHelper, mockScopesService)

  //TODO : ADD Tests

  "SandboxCorporationTaxController" should {
    "not need an bearer token to operate" in {
      val result = await(sandboxController.corporationTax(CorporationTaxSandboxData.sandboxMatchIdUUID)(fakeRequest))

      jsonBodyOf(result) shouldBe Json.toJson(CorporationTaxSandboxData.sandboxReturnData)
    }

  }

}