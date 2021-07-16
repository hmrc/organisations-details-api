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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import controllers.Assets.{BAD_REQUEST, NOT_FOUND}
import org.mockito.ArgumentMatchers.{any, refEq, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.controllers.{LiveCorporationTaxController, SandboxCorporationTaxController}
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.{AccountingPeriod, CorporationTaxResponse}
import uk.gov.hmrc.organisationsdetailsapi.sandbox.CorporationTaxSandboxData
import uk.gov.hmrc.organisationsdetailsapi.services.{LiveCorporationTaxService, SandboxCorporationTaxService, ScopesService}
import utils.TestSupport

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CorporationTaxControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with TestSupport {

  implicit val sys: ActorSystem = ActorSystem("MyTest")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val sampleCorrelationIdHeader = ("CorrelationId" -> sampleCorrelationId)

  val sampleMatchId = "32696d72-6216-475f-b213-ba76921cf459"
  val sampleMatchIdUUID = UUID.fromString(sampleMatchId)

  private val fakeRequest = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]

  private val mockLiveCorporationTaxService = mock[LiveCorporationTaxService]
  private val sandboxCorporationTaxService = new SandboxCorporationTaxService()

  private val liveController = new LiveCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), mockLiveCorporationTaxService, mockAuditHelper, mockScopesService)
  private val sandboxController = new SandboxCorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(), sandboxCorporationTaxService, mockAuditHelper, mockScopesService)

  private val sampleResponse = CorporationTaxResponse(
    dateOfRegistration = Some(LocalDate.of(2014, 4, 21)),
    taxSolvencyStatus = Some("V"),
    periods = Some(Seq(
      AccountingPeriod(
        accountingPeriodStartDate = Some(LocalDate.of(2017, 4, 6)),
        accountingPeriodEndDate = Some(LocalDate.of(2017, 10, 5)),
        turnover = Some(38390)
      ),
      AccountingPeriod(
        accountingPeriodStartDate = Some(LocalDate.of(2017, 10, 6)),
        accountingPeriodEndDate = Some(LocalDate.of(2018, 4, 5)),
        turnover = Some(2340)
      )
    ))
  )

  "SandboxCorporationTaxController" should {
    "not need an bearer token to operate" in {

      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      val result = await(sandboxController.corporationTax(CorporationTaxSandboxData.sandboxMatchIdUUID)(fakeRequest))
      jsonBodyOf(result) shouldBe
        Json.parse(
          """
            |{
            |    "taxSolvencyStatus": "V",
            |    "_links": {
            |        "self": {
            |            "href": "/organisations/details/corporation-tax?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
            |        }
            |    },
            |    "dateOfRegistration": "2015-04-21",
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

      verify(mockAuthConnector, times(0)).authorise(any(), any())(any(), any())
    }

    "return not found when match Id does not match sandbox match id" in {

      val matchId = UUID.fromString("7179e4d5-625d-4483-9ec6-63940ff61e63")
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      val response = await(sandboxController.corporationTax(matchId)(fakeRequest))

      status(response) shouldBe NOT_FOUND
    }

    "fail when correlationId is not provided" in {
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))
      val exception = intercept[BadRequestException](await(sandboxController.corporationTax(CorporationTaxSandboxData.sandboxMatchIdUUID)(FakeRequest())))
      exception.message shouldBe "CorrelationId is required"
    }

    "fail when correlationId is not malformed" in {
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))
      val exception = intercept[BadRequestException](await(sandboxController.corporationTax(CorporationTaxSandboxData.sandboxMatchIdUUID)(FakeRequest().withHeaders("CorrelationId" -> "Not a valid correlationId"))))
      exception.message shouldBe "Malformed CorrelationId"
    }

  }

  "LiveCorporationTaxController" should {

    "return data when called successfully with a valid request" in {
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      when(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
        .thenReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))

      when(mockLiveCorporationTaxService.get(eqTo(sampleMatchIdUUID), eqTo("corporation-tax"), eqTo(Seq("test-scope")))(any(), any(), any()))
        .thenReturn(Future.successful(sampleResponse))

      val result = await(liveController.corporationTax(sampleMatchIdUUID)(fakeRequest))


      jsonBodyOf(result) shouldBe
        Json.parse(
          """
            |{
            |    "taxSolvencyStatus": "V",
            |    "_links": {
            |        "self": {
            |            "href": "/organisations/details/corporation-tax?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
            |        }
            |    },
            |    "dateOfRegistration": "2014-04-21",
            |    "periods": [
            |        {
            |            "accountingPeriodStartDate": "2017-04-06",
            |            "accountingPeriodEndDate": "2017-10-05",
            |            "turnover": 38390
            |        },
            |        {
            |            "accountingPeriodStartDate": "2017-10-06",
            |            "accountingPeriodEndDate": "2018-04-05",
            |            "turnover": 2340
            |        }
            |    ]
            |} """.stripMargin
        )

    }

    "fail when correlationId is not provided" in {
      when(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
        .thenReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      val response = await(liveController.corporationTax(sampleMatchIdUUID)(FakeRequest()))

      status(response) shouldBe BAD_REQUEST
      jsonBodyOf(response) shouldBe Json.parse(
        """
          |{
          |  "code": "INVALID_REQUEST",
          |  "message": "CorrelationId is required"
          |}
          |""".stripMargin
      )
    }

    "fail when correlationId is not malformed" in {
      when(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
        .thenReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      val response = await(liveController.corporationTax(sampleMatchIdUUID)(FakeRequest().withHeaders("CorrelationId" -> "Not a valid correlationId")))

      status(response) shouldBe BAD_REQUEST
      jsonBodyOf(response) shouldBe Json.parse(
        """
          |{
          |  "code": "INVALID_REQUEST",
          |  "message": "Malformed CorrelationId"
          |}
          |""".stripMargin
      )
    }
  }

}