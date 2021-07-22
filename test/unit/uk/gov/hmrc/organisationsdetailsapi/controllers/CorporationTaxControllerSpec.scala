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
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.controllers.CorporationTaxController
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.{AccountingPeriod, CorporationTaxResponse}
import uk.gov.hmrc.organisationsdetailsapi.services.{CorporationTaxService, ScopesService}
import utils.TestSupport
import java.time.LocalDate
import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CorporationTaxControllerSpec
  extends AnyWordSpec
  with Matchers
  with MockitoSugar
  with TestSupport
  with BeforeAndAfterEach {

  implicit val sys: ActorSystem = ActorSystem("MyTest")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  private val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  private val sampleCorrelationIdHeader = ("CorrelationId" -> sampleCorrelationId)

  private val sampleMatchId = "32696d72-6216-475f-b213-ba76921cf459"
  private val sampleMatchIdUUID = UUID.fromString(sampleMatchId)

  private val fakeRequest = FakeRequest("GET", "/").withHeaders(sampleCorrelationIdHeader)

  private val mockAuthConnector = mock[AuthConnector]
  private val mockAuditHelper = mock[AuditHelper]
  private val mockScopesService = mock[ScopesService]

  private val mockCorporationTaxService = mock[CorporationTaxService]

  private val liveController = new CorporationTaxController(mockAuthConnector, Helpers.stubControllerComponents(),
    mockCorporationTaxService, mockAuditHelper, mockScopesService)

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

  override def beforeEach(): Unit = {
    reset(mockAuditHelper)
  }

  "LiveCorporationTaxController" should {

    "return data when called successfully with a valid request" in {
      when(mockScopesService.getEndPointScopes("corporation-tax")).thenReturn(Seq("test-scope"))

      when(mockAuthConnector.authorise(eqTo(Enrolment("test-scope")), refEq(Retrievals.allEnrolments))(any(), any()))
        .thenReturn(Future.successful(Enrolments(Set(Enrolment("test-scope")))))

      when(mockCorporationTaxService.get(refEq(sampleMatchIdUUID), eqTo("corporation-tax"), eqTo(Set("test-scope")))(any(), any(), any()))
        .thenReturn(Future.successful(sampleResponse))

      val result = await(liveController.corporationTax(sampleMatchIdUUID)(fakeRequest))

      verify(mockAuditHelper, times(1)).auditCorporationTaxApiResponse(
        any(), any(), any(), any(), any(), any())(any())

      jsonBodyOf(result) shouldBe
        Json.parse(
          s"""
            |{
            |    "taxSolvencyStatus": "V",
            |    "_links": {
            |        "self": {
            |            "href": "/organisations/details/corporation-tax?matchId=$sampleMatchIdUUID"
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

      verify(mockAuditHelper, times(1)).auditApiFailure(
        any(), any(), any(), any(), any())(any())

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

      verify(mockAuditHelper, times(1)).auditApiFailure(
        any(), any(), any(), any(), any())(any())

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