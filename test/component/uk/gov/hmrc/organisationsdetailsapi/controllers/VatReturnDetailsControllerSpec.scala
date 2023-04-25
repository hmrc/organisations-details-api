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

package component.uk.gov.hmrc.organisationsdetailsapi.controllers

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.{AuthStub, BaseSpec, IfStub, OrganisationsMatchingApiStub}
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK, UNAUTHORIZED}
import play.api.libs.json.Json
import scalaj.http.{Http, HttpOptions}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{IfTaxYear, IfVatReturn, IfVatReturnDetailsResponse}
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.{OrganisationMatch, OrganisationVatMatch}
import uk.gov.hmrc.organisationsdetailsapi.domain.vat.{TaxYear, VatReturnDetailsResponse, VatReturn}

import java.util.UUID

class VatReturnDetailsControllerSpec extends BaseSpec{

  val matchId: UUID = UUID.fromString("ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f")
  val utr = "1234567890"
  val scopes = List("read:organisations-details-ho-ssp", "read:organisations-details-ho-suv")
  val validMatch: OrganisationVatMatch = OrganisationVatMatch(matchId, "1234567890")
  val validVatIfResponse: IfVatReturnDetailsResponse = IfVatReturnDetailsResponse(
    vrn = Some("1234567890") ,
    appDate= Some("20160425"),
    taxYears = Some(Seq(
      IfTaxYear(
        taxYear = Some("2019"),
        vatReturns = Some(Seq(
          IfVatReturn(
            calendarMonth = Some(1),
            liabilityMonth = Some(10),
            numMonthsAssessed = Some(5),
            box6Total = Some(6542),
            returnType = Some("Regular Return"),
            source = Some("VMF")
          )
        ))
      )
    )
    )
  )

  private val sampleValidRequest = VatReturnDetailsResponse(
    vrn = Some("1234567890") ,
    taxYears = Some(Seq(
      TaxYear(
        taxYear = Some("2019"),
        vatReturns = Some(Seq(
          VatReturn(
            calendarMonth = Some(1),
            liabilityMonth = Some(10),
            numMonthsAssessed = Some(5),
            box6Total = Some(6542),
            returnType = Some("Regular Return")
          )
        ))
      )
    )
    )
  )

  Feature("vat") {

    Scenario("a valid request is made for an existing match") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingRecord(matchId.toString, validMatch.vrn)

      Given("Data found in IF")
      IfStub.searchVatReturnDetails(validMatch.vrn, validVatIfResponse)

      val x = IfStub.searchVatReturnDetails(validMatch.vrn, validVatIfResponse)
      println(x + "HEYYYYYYYYYYY")

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
        .option(HttpOptions.readTimeout(10000))
        .asString

      println(response + "RESPONSE HERE")

//      response.code mustBe OK

      Json.parse(response.body) mustBe Json.parse(
        """
          |{
          |  "vrn": "123456789",
          |  "appDate": "20160425",
          |  "taxYears": [
          |    {
          |      "taxYear": "2019",
          |      "vatReturns": [
          |        {
          |          "calendarMonth": 1,
          |          "liabilityMonth": 10,
          |          "numMonthsAssessed": 5,
          |          "box6Total": 6542,
          |          "returnType": "Regular Return",
          |          "source": "VMF"
          |        },
          |        {
          |          "calendarMonth": 2,
          |          "liabilityMonth": 11,
          |          "numMonthsAssessed": 6,
          |          "box6Total": 12345.23,
          |          "returnType": "Regular Return",
          |          "source": "VMF"
          |        }
          |      ]
          |    },
          |    {
          |      "taxYear": "2020",
          |      "vatReturns": [
          |        {
          |          "calendarMonth": 12,
          |          "liabilityMonth": 9,
          |          "numMonthsAssessed": 4,
          |          "box6Total": 2344,
          |          "returnType": "Regular Return",
          |          "source": "ADR(ETMP)"
          |        },
          |        {
          |          "calendarMonth": 6,
          |          "liabilityMonth": 3,
          |          "numMonthsAssessed": 1,
          |          "box6Total": 123.23,
          |          "returnType": "Regular Return",
          |          "source": "ADR(ETMP)"
          |        }
          |      ]
          |    }
          |  ]
          |}
          |
          |""".stripMargin)
    }
  }

  Scenario("a valid request is made for an expired match") {
    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    When("the API is invoked")
    val response = Http(s"$serviceUrl/vat?matchId=$matchId")
      .headers(requestHeaders(acceptHeaderVP1))
      .option(HttpOptions.readTimeout(10000))
      .asString

    response.code mustBe NOT_FOUND
    Json.parse(response.body) mustBe Json.obj(
      "code" -> "NOT_FOUND",
      "message" -> "The resource can not be found"
    )
  }

  Scenario("a request is made with a missing match id") {
    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    When("the API is invoked")
    val response = Http(s"$serviceUrl/corporation-tax/")
      .headers(requestHeaders(acceptHeaderVP1))
      .asString

    response.code mustBe BAD_REQUEST

    Json.parse(response.body) mustBe Json.obj(
      "code" -> "INVALID_REQUEST",
      "message" -> "matchId is required"
    )
  }

  Scenario("a request is made with a malformed match id") {
    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    When("the API is invoked")
    val response = Http(s"$serviceUrl/corporation-tax?matchId=foo")
      .headers(requestHeaders(acceptHeaderVP1))
      .asString

    response.code mustBe BAD_REQUEST

    Json.parse(response.body) mustBe Json.obj(
      "code" -> "INVALID_REQUEST",
      "message" -> "matchId format is invalid"
    )
  }

  Scenario("a request is made with a missing correlation id") {
    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    When("the API is invoked")
    val response = Http(s"$serviceUrl/corporation-tax?matchId=$matchId")
      .headers(requestHeadersInvalid(acceptHeaderVP1))
      .asString

    response.code mustBe BAD_REQUEST

    Json.parse(response.body) mustBe Json.obj(
      "code" -> "INVALID_REQUEST",
      "message" -> "CorrelationId is required"
    )
  }

  Scenario("a request is made with a malformed correlation id") {
    Given("A valid privileged Auth bearer token")
    AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

    When("the API is invoked")
    val response = Http(s"$serviceUrl/corporation-tax?matchId=$matchId")
      .headers(requestHeadersMalformed(acceptHeaderVP1))
      .asString

    response.code mustBe BAD_REQUEST

    Json.parse(response.body) mustBe Json.obj(
      "code" -> "INVALID_REQUEST",
      "message" -> "Malformed CorrelationId"
    )
  }
}



