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

import java.time.LocalDate
import play.api.libs.json.Json
import play.api.test.Helpers._

import java.util.UUID
import component.uk.gov.hmrc.organisationsdetailsapi.stubs.{AuthStub, BaseSpec, IfStub, OrganisationsMatchingApiStub}
import scalaj.http.{Http, HttpOptions}
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.AccountingPeriod
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{AccountingPeriod => IfAccountingPeriod}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetailsResponse

class CorporationTaxControllerSpec extends BaseSpec {

  val matchId: UUID = UUID.fromString("ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f")
  val utr = "1234567890"
  val scopes = List("read:organisations-details-ho-ssp", "read:organisations-details-ho-suv")
  val period1: AccountingPeriod = AccountingPeriod(Some(LocalDate.of(2018, 4, 6)), Some(LocalDate.of(2018, 10, 5)), Some(38390))
  val period2: AccountingPeriod = AccountingPeriod(Some(LocalDate.of(2018, 10, 6)), Some(LocalDate.of(2018, 4, 5)), Some(2340))
  val taxSolvencyStatus: Some[String] = Some("V")
  val dateOfRegistration: Some[LocalDate] = Some(LocalDate.of(2014, 4, 21))
  val validMatch: OrganisationMatch = OrganisationMatch(matchId, "1234567890")
  val validCtIfResponse: CorporationTaxReturnDetailsResponse = CorporationTaxReturnDetailsResponse(
    Some(validMatch.utr),
    Some("2015-04-21"),
    Some("V"),
    Some(Seq(
      IfAccountingPeriod(
        Some("2018-04-06"),
        Some("2018-10-05"),
        Some(38390)
      ),
      IfAccountingPeriod(
        Some("2018-10-06"),
        Some("2019-04-05"),
        Some(2340)
      ),
    ))
  )

  Feature("cotax") {

    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingRecord(matchId.toString, validMatch.utr)

      Given("Data found in IF")
      IfStub.searchCtReturnDetails(validMatch.utr, validCtIfResponse)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .option(HttpOptions.readTimeout(10000))
        .asString

      response.code mustBe OK

      Json.parse(response.body) mustBe Json.parse(
        """
          |{
          |    "taxSolvencyStatus": "V",
          |    "_links": {
          |        "self": {
          |            "href": "/organisations/details/corporation-tax?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
          |        }
          |    },
          |    "dateOfRegistration": "2015-04-21",
          |    "accountingPeriods": [
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

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .option(HttpOptions.readTimeout(10000))
        .asString

      response.code mustBe NOT_FOUND
      Json.parse(response.body) mustBe Json.obj(
        "code" -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )
    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/corporation-tax?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code mustBe UNAUTHORIZED
      Json.parse(response.body) mustBe Json.obj(
        "code" -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
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
}