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
import play.api.libs.json.Json
import play.api.test.Helpers._
import scalaj.http.{Http, HttpOptions}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{SelfAssessmentReturnDetailResponse, TaxYear}
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.OrganisationMatch

import java.util.UUID

class SelfAssessmentControllerSpec extends BaseSpec {

  val matchId: UUID = UUID.fromString("ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f")
  val utr = "1234567890"
  val scopes = List("read:organisations-details-ho-ssp", "read:organisations-details-ho-suv")
  val validMatch: OrganisationMatch = OrganisationMatch(matchId, "1234567890")
  val ifData: SelfAssessmentReturnDetailResponse = SelfAssessmentReturnDetailResponse(
    utr = Some(utr),
    startDate = Some("2020-01-01"),
    taxPayerType = Some("INDIVIDUAL"),
    taxSolvencyStatus = Some("I"),
    taxYears = Some(Seq(
      TaxYear(
        taxyear = Some("2020"),
        businessSalesTurnover = Some(50000))
    ))
  )


  Feature("sa") {

    Scenario("a valid request is made for an existing match") {

      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingRecord(matchId.toString, validMatch.utr)

      Given("Data found in IF")
      IfStub.searchSaDetails(validMatch.utr, ifData)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .option(HttpOptions.readTimeout(10000))
        .asString

      response.code mustBe OK

      Json.parse(response.body) mustBe Json.parse(
        """{
          | "selfAssessmentStartDate":"2020-01-01",
          | "taxSolvencyStatus":"I",
          | "_links":
          |   {"self": {
          |     "href":"/organisations/details/self-assessment?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
          |     }
          |   },
          | "taxReturns": [{
          |   "totalBusinessSalesTurnover":50000,
          |   "taxYear":"2020"
          | }]
          | }""".stripMargin)
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/self-assessment?matchId=$matchId")
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
      val response = Http(s"$serviceUrl/self-assessment?matchId=$matchId")
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
      val response = Http(s"$serviceUrl/self-assessment")
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
      val response = Http(s"$serviceUrl/self-assessment?matchId=foo")
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
      val response = Http(s"$serviceUrl/self-assessment?matchId=$matchId")
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
      val response = Http(s"$serviceUrl/self-assessment?matchId=$matchId")
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