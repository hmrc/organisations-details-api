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

import component.uk.gov.hmrc.organisationsdetailsapi.errorResponse
import component.uk.gov.hmrc.organisationsdetailsapi.stubs.{ AuthStub, BaseSpec, IfStub, OrganisationsMatchingApiStub }
import play.api.http.Status.{ BAD_REQUEST, NOT_FOUND, OK, TOO_MANY_REQUESTS }
import play.api.libs.json.{ JsObject, Json }
import scalaj.http.Http
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{ IfTaxYear, IfVatReturn, IfVatReturnDetailsResponse }
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.OrganisationVatMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.vat.{ TaxYear, VatReturn, VatReturnDetailsResponse }

import java.util.UUID
import scala.util.Random

class VatReturnDetailsControllerSpec extends BaseSpec {

  class Fixture {
    val matchId: UUID = UUID.randomUUID()
    val vrn = (1 to 10).map(_ => Random.nextInt(10)).mkString("")
    val scopes = List("read:organisations-details-ho-suv")
    val validMatch: OrganisationVatMatch = OrganisationVatMatch(matchId, vrn)

    val validVatIfResponse: IfVatReturnDetailsResponse = IfVatReturnDetailsResponse(
      vrn = Some(vrn),
      appDate = Some("20160425"),
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
          )
          )
        )
      )
      )
    )

    val validResponse = VatReturnDetailsResponse(
      vrn = Some(vrn),
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
  }

  Feature("vat") {

    Scenario("a valid request is made for an existing match") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingVatRecord(matchId, validMatch.vrn)

      Given("Data found in IF")
      IfStub.searchVatReturnDetails(validMatch.vrn, validVatIfResponse)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      response.code mustBe OK

      Json.parse(response.body) mustBe Json.obj(
        "_links" -> Json.obj("self" -> Json.obj("href" -> s"/organisations/details/vat?matchId=$matchId"))
      ) ++ Json.toJson(validResponse).asInstanceOf[JsObject]
    }

    Scenario("a valid request is made for an expired match") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("There is no match for the given matchId")
      OrganisationsMatchingApiStub.hasNoMatchingVatRecord(matchId, vrn)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      response.code mustBe NOT_FOUND
      Json.parse(response.body) mustBe errorResponse("NOT_FOUND", "The resource can not be found")
    }

    Scenario("a request is made with a missing match id") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat/")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe errorResponse("INVALID_REQUEST", "matchId is required")
    }

    Scenario("a request is made with a malformed match id") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=foo")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe errorResponse("INVALID_REQUEST", "matchId format is invalid")
    }

    Scenario("a request is made with a missing correlation id") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeadersInvalid(acceptHeaderVP1))
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe errorResponse("INVALID_REQUEST", "CorrelationId is required")
    }

    Scenario("a request is made with a malformed correlation id") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeadersMalformed(acceptHeaderVP1))
        .asString

      response.code mustBe BAD_REQUEST

      Json.parse(response.body) mustBe errorResponse("INVALID_REQUEST", "Malformed CorrelationId")
    }

    Scenario("a request is made with a vrn which has no records") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingVatRecord(matchId, validMatch.vrn)

      Given("Data not found in IF")
      IfStub.searchVatReturnDetailsNotFound(validMatch.vrn)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      println(response.body)

      response.code mustBe NOT_FOUND

      Json.parse(response.body) mustBe errorResponse("NOT_FOUND", "The resource can not be found")
    }

    Scenario("The api is called more frequently than it was configured for") {
      val f = new Fixture
      import f._
      Given("A valid privileged Auth bearer token")
      AuthStub.willAuthorizePrivilegedAuthToken(authToken, scopes)

      Given("Matching Api has matching record")
      OrganisationsMatchingApiStub.hasMatchingVatRecord(matchId, validMatch.vrn)

      Given("IF returns Too many request exception")
      IfStub.searchVatReturnDetailsNotFoundRateLimited(validMatch.vrn)

      When("the API is invoked")
      val response = Http(s"$serviceUrl/vat?matchId=$matchId")
        .headers(requestHeaders(acceptHeaderVP1))
        .asString

      response.code mustBe TOO_MANY_REQUESTS
      Json.parse(response.body) mustBe errorResponse("TOO_MANY_REQUESTS", "Rate limit exceeded")
    }
  }
}
