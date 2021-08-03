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

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.{AuthStub, BaseSpec, IfStub, OrganisationsMatchingApiStub}
import controllers.Assets.{BAD_REQUEST, OK, UNAUTHORIZED}
import play.api.libs.json.Json
import scalaj.http.{Http, HttpOptions}
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{Count, EmployeeCountRequest, EmployeeCountResponse, PayeReferenceAndCount}
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.{NumberOfEmployeesRequest, PayeReference => RequestPayeReference}

import java.util.UUID

class NumberOfEmployeesControllerSpec extends BaseSpec {
  private val matchId = UUID.fromString("ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f")
  private val utr = "1234567890"
  private val scopes = List("read:organisations-details-ho-ssp")
  private val validMatch = OrganisationMatch(matchId, utr)

  private val validNumberOfEmployeesIfResponse = EmployeeCountResponse(
    Some("2019-10-01"),
    Some("2020-04-05"),
    Some(Seq(
      PayeReferenceAndCount(
        Some("456"),
        Some("RT882d"),
        Some(Seq(
          Count(
            Some("2019-10"),
            Some(1234)
          )
        ))
      )
    ))
  )

  private val sampleValidRequest = NumberOfEmployeesRequest(
    "2019-10-01",
    "2020-04-05",
    Seq(
      RequestPayeReference("456", "RT882d")
    )
  )

  private val ifRequest = EmployeeCountRequest.createFromRequest(sampleValidRequest)

  Feature("Number of Employees") {
    Scenario("a valid request is made for an existing match") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingRecord(matchId.toString, validMatch.utr)

      Given("Data found in IF")
      IfStub.searchNumberOfEmployees(validMatch.utr, validNumberOfEmployeesIfResponse, ifRequest)

      When("The API is invoked")
      val response = Http(s"$serviceUrl/number-of-employees?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
        .option(HttpOptions.readTimeout(10000))
        .asString

      response.code mustBe OK

      Json.parse(response.body) mustBe Json.parse(
        """
          |{
          |    "_links": {
          |        "self": {
          |            "href": "/organisations/details/number-of-employees?matchId=ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
          |        }
          |    },
          |    "employeeCounts": [
          |        {
          |            "payeReference": "456/RT882d",
          |            "counts": [
          |                {
          |                    "numberOfEmployees": 1234,
          |                    "dateOfCount": "2019-10"
          |                }
          |            ]
          |        }
          |    ]
          |}
          |""".stripMargin)

    }

    Scenario("not authorized") {

      Given("an invalid privileged Auth bearer token")
      AuthStub.willNotAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/number-of-employees?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
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
      val response = Http(s"$serviceUrl/number-of-employees/")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
        .asString

      response.code mustBe BAD_REQUEST
    }

    Scenario("a request is made with a malformed match id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/number-of-employees?matchId=foo")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "statusCode" -> 400,
        "message" -> "bad request, cause: REDACTED"
      )
    }

    Scenario("a request is made with a missing correlation id") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/number-of-employees?matchId=$matchId")
        .headers(requestHeadersInvalid(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
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
      val response = Http(s"$serviceUrl/number-of-employees?matchId=$matchId")
        .headers(requestHeadersMalformed(acceptHeaderVP1))
        .postData(Json.toJson(sampleValidRequest).toString())
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> "Malformed CorrelationId"
      )
    }

    Scenario("Invalid data posted with request") {
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("The API is invoked")
      val response = Http(s"$serviceUrl/number-of-employees?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .postData(Json.obj("foo" -> "bar").toString())
        .option(HttpOptions.readTimeout(10000))
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe Json.obj(
        "code" -> "INVALID_REQUEST",
        "message" -> "Malformed payload"
      )

    }

  }
}
