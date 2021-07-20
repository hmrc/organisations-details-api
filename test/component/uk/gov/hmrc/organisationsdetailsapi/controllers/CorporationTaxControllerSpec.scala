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

package component.uk.gov.hmrc.organisationsdetailsapi.controllers

import java.time.LocalDate

import play.api.libs.json.Json
import play.api.test.Helpers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import uk.gov.hmrc.organisationsdetailsapi.domain.models.{CtMatch, SaMatch}
import uk.gov.hmrc.organisationsdetailsapi.domain.ogd.{CtMatchingRequest, SaMatchingRequest}
import java.util.UUID

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.{AuthStub, BaseSpec}
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.AccountingPeriod

import scala.concurrent.Await.result

class CorporationTaxControllerSpec extends BaseSpec  {

  val matchId   = UUID.randomUUID()
  val scopes    = List("read:organisations-details-paye")
  val period1   = AccountingPeriod(Some(LocalDate.of(2018, 4, 6)), Some(LocalDate.of(2018, 10, 5)), Some(38390))
  val period2   = AccountingPeriod(Some(LocalDate.of(2018, 10, 6)), Some(LocalDate.of(2018, 4, 5)), Some(2340))
  val taxSolvencyStatus   = Some("V")
  val dateOfRegistration   = Some(LocalDate.of(2014, 4, 21))

  Feature("cotax") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid nino match exist")
      result(mongoRepository.cache(matchId.toString, "organisations-matching", ctMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
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
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "statusCode"    -> 400,
        "message" -> "bad request"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/corporation-tax/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }
  }

  Feature("self-assessment") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      And("A valid nino match exist")
      result(mongoRepository.cache(matchId.toString, "organisations-matching", saMatch), timeout)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe OK

      Json.parse(response.body) shouldBe Json.parse(
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
    }

    Scenario("a valid request is made for an expired match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "NOT_FOUND",
        "message" -> "The resource can not be found"
      )

    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      Then("the response status should be 401 (unauthorized)")
      response.code shouldBe UNAUTHORIZED
      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "UNAUTHORIZED",
        "message" -> "Bearer token is missing or not authorized"
      )
    }

    Scenario("a request is made with a missing match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe NOT_FOUND
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/foo")
        .headers(requestHeaders(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "statusCode"    -> 400,
        "message" -> "bad request"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeadersInvalid(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "CorrelationId is required"
      )
    }

    Scenario("a request is made with a malformed correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/matching/self-assessment/$matchId")
        .headers(requestHeadersMalformed(acceptHeaderP1))
        .asString

      response.code shouldBe BAD_REQUEST

      Json.parse(response.body) shouldBe Json.obj(
        "code"    -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }
  }
}
}