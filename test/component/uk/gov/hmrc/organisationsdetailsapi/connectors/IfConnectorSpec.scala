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

package component.uk.gov.hmrc.organisationsdetailsapi.connectors

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import org.mockito.ArgumentMatchers.{any, matches, contains}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, InternalServerException, NotFoundException}
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.connectors.IfConnector
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountResponse._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.SelfAssessmentReturnDetail._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{CorporationTaxReturnDetailsResponse, EmployeeCountResponse, SelfAssessmentReturnDetailResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.{IfHelpers, TestSupport}

import scala.concurrent.ExecutionContext


class IfConnectorSpec
    extends AnyWordSpec
    with BeforeAndAfterEach
    with TestSupport
    with MockitoSugar
    with Matchers
    with GuiceOneAppPerSuite
    with IfHelpers {

  val stubPort = sys.env.getOrElse("WIREMOCK", "11122").toInt
  val stubHost = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val integrationFrameworkAuthorizationToken = "IF_TOKEN"
  val integrationFrameworkEnvironment = "IF_ENVIRONMENT"
  val clientId = "CLIENT_ID"

  def externalServices: Seq[String] = Seq.empty

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .bindings(bindModules: _*)
    .configure(
      "cache.enabled"  -> false,
      "microservice.services.integration-framework.host" -> "localhost",
      "microservice.services.integration-framework.port" -> "11122",
      "microservice.services.integration-framework.authorization-token" -> integrationFrameworkAuthorizationToken,
      "microservice.services.integration-framework.environment" -> integrationFrameworkEnvironment
    )
    .build()

  implicit val ec: ExecutionContext =
    fakeApplication.injector.instanceOf[ExecutionContext]

  trait Setup {
    val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
    val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
    val sampleCorrelationIdHeader: (String, String) = ("CorrelationId" -> sampleCorrelationId)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val config: ServicesConfig = fakeApplication.injector.instanceOf[ServicesConfig]
    val httpClient: HttpClient = fakeApplication.injector.instanceOf[HttpClient]
    val auditHelper: AuditHelper = mock[AuditHelper]

    val underTest = new IfConnector(config, httpClient, auditHelper)
  }

  override def beforeEach() {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach() {
    wireMockServer.stop()
  }

  val utr = "1234567890"

  val taxReturn  = createValidCorporationTaxReturnDetails()
  val saReturn = createValidSelfAssessmentReturnDetails()
  val employeeCountRequest = createValidEmployeeCountRequest()
  val employeeCountResponse = createValidEmployeeCountResponse()

  val invalidTaxReturn  = createValidCorporationTaxReturnDetails().copy(utr = "")
  val invalidSaReturn = createValidSelfAssessmentReturnDetails().copy(utr = "")
  val invalidEmployeeCountRequest = createValidEmployeeCountRequest().copy(startDate = "")
  val invalidEmployeeCountResponse = createValidEmployeeCountResponse().copy(startDate = "")

  "fetch foo" should {

    "Fail when IF returns an error" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
          .willReturn(aResponse().withStatus(500)))

      intercept[InternalServerException] {
        await(
          underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())

    }

    "Fail when IF returns a bad request" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
          .willReturn(aResponse().withStatus(400).withBody("BAD_REQUEST")))

      intercept[InternalServerException] {
        await(
          underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when IF returns a NOT_FOUND and return error with empty body" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
          .willReturn(aResponse().withStatus(404)))

      intercept[NotFoundException]{
        await(
          underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }
      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
    }

    "Fail when IF returns a NOT_DATA_FOUND and return error in body" in new Setup {

      Mockito.reset(underTest.auditHelper)

      stubFor(
        get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
          .willReturn(aResponse().withStatus(404).withBody(Json.stringify(Json.parse(
            """{
              |  "failures": [
              |    {
              |      "code": "NO_DATA_FOUND",
              |      "reason": "The remote endpoint has indicated no data was found for the provided utr."
              |    }
              |  ]
              |}""".stripMargin)))))

      intercept[NotFoundException] {
        await(
          underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }
      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), contains("""NO_DATA_FOUND"""))(any())
    }

    "getCtReturnDetails" should {

      "successfully parse valid CorporationTaxReturnDetailsResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonResponse = Json.prettyPrint(Json.toJson(taxReturn))

        stubFor(
          get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
            .willReturn(okJson(jsonResponse)))

        val result: CorporationTaxReturnDetailsResponse = await(
          underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )

        result shouldBe taxReturn

        verify(underTest.auditHelper,
          times(0)).auditIfApiFailure(any(), any(), any(), any(), any())(any())

      }

      "successfully parse invalid CorporationTaxReturnDetailsResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonResponse = Json.prettyPrint(Json.toJson(invalidTaxReturn))

        stubFor(
          get(urlPathMatching(s"/organisations/corporation-tax/$utr/return/details"))
            .willReturn(okJson(jsonResponse)))


        intercept[InternalServerException] {
          await(
            underTest.getCtReturnDetails(UUID.randomUUID().toString, utr)(
              hc,
              FakeRequest().withHeaders(sampleCorrelationIdHeader),
              ec
            )
          )
        }

        verify(underTest.auditHelper,
          times(1)).auditIfApiFailure(any(), any(), any(), any(), matches("^Error parsing IF response"))(any())

      }
    }

    "getSaReturnDetails" should {

      "successfully parse valid SelfAssessmentReturnDetailsResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonResponse = Json.prettyPrint(Json.toJson(saReturn))

        stubFor(
          get(urlPathMatching(s"/organisations/self-assessment/${utr}/return/details"))
            .willReturn(okJson(jsonResponse)))

        val result:SelfAssessmentReturnDetailResponse = await(
          underTest.getSaReturnDetails(UUID.randomUUID().toString, utr)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )

        result shouldBe saReturn

        verify(underTest.auditHelper,
          times(0)).auditIfApiFailure(any(), any(), any(), any(), any())(any())
      }

      "successfully parse invalid SelfAssessmentReturnDetailsResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonResponse = Json.prettyPrint(Json.toJson(invalidSaReturn))

        stubFor(
          get(urlPathMatching(s"/organisations/self-assessment/${utr}/return/details"))
            .willReturn(okJson(jsonResponse)))

        intercept[InternalServerException] {
          await(
            underTest.getSaReturnDetails(UUID.randomUUID().toString, utr)(
              hc,
              FakeRequest().withHeaders(sampleCorrelationIdHeader),
              ec
            )
          )
        }

        verify(underTest.auditHelper,
          times(1)).auditIfApiFailure(any(), any(), any(), any(), matches("^Error parsing IF response"))(any())

      }
    }

    "getEmployeeCount" should {

      "successfully parse valid EmployeeCountResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonRequest = Json.prettyPrint(Json.toJson(employeeCountRequest))
        val jsonResponse = Json.prettyPrint(Json.toJson(employeeCountResponse))

        stubFor(
          post(urlPathMatching(s"/organisations/employers/employee/counts"))
            .withRequestBody(new EqualToJsonPattern(jsonRequest, true, true))
            .willReturn(okJson(jsonResponse)))

        val result: EmployeeCountResponse = await(
          underTest.getEmployeeCount(UUID.randomUUID().toString, utr, employeeCountRequest)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )

        result shouldBe employeeCountResponse

        verify(underTest.auditHelper,
          times(0)).auditIfApiFailure(any(), any(), any(), any(), any())(any())

      }

      "successfully audit and consume invalid EmployeeCountResponse from IF response" in new Setup {

        Mockito.reset(underTest.auditHelper)

        val jsonRequest = Json.prettyPrint(Json.toJson(employeeCountRequest))
        val jsonResponse = Json.prettyPrint(Json.toJson(invalidEmployeeCountResponse))

        stubFor(
          post(urlPathMatching(s"/organisations/employers/employee/counts"))
            .withRequestBody(new EqualToJsonPattern(jsonRequest, true, true))
            .willReturn(okJson(jsonResponse)))

        intercept[InternalServerException] {
          await(
            underTest.getEmployeeCount(UUID.randomUUID().toString, utr, employeeCountRequest)(
              hc,
              FakeRequest().withHeaders(sampleCorrelationIdHeader),
              ec
            )
          )
        }

        verify(underTest.auditHelper,
          times(1)).auditIfApiFailure(any(), any(), any(), any(), matches("^Error parsing IF response"))(any())

      }
    }

    "successfully audit and consume invalid EmployeeCountRequest from IF response" in new Setup {

      Mockito.reset(underTest.auditHelper)

      val jsonRequest = Json.prettyPrint(Json.toJson(employeeCountRequest))
      val jsonResponse = """{
                            |  "failures": [
                            |    {
                            |      "code": "INVALID_PAYLOAD",
                            |      "reason": "Submission has not passed validation. Invalid payload."
                            |    }
                            |  ]
                            |}""".stripMargin

      stubFor(
        post(urlPathMatching(s"/organisations/employers/employee/counts"))
          .withRequestBody(new EqualToJsonPattern(jsonRequest, true, true))
          .willReturn(aResponse().withStatus(400).withBody(jsonResponse)))

      intercept[InternalServerException] {
        await(
          underTest.getEmployeeCount(UUID.randomUUID().toString, utr, employeeCountRequest)(
            hc,
            FakeRequest().withHeaders(sampleCorrelationIdHeader),
            ec
          )
        )
      }

      verify(underTest.auditHelper,
        times(1)).auditIfApiFailure(any(), any(), any(), any(), contains("INVALID_PAYLOAD"))(any())
    }
  }
}

