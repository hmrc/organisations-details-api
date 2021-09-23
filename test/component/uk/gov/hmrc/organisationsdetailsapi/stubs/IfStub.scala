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

package component.uk.gov.hmrc.organisationsdetailsapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, get, post, urlPathEqualTo}
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{CorporationTaxReturnDetailsResponse, SelfAssessmentReturnDetailResponse, EmployeeCountRequest, EmployeeCountResponse}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.SelfAssessmentReturnDetail._


object IfStub extends MockHost(8443) {

  def searchNumberOfEmployees(utr: String, result: EmployeeCountResponse, request: EmployeeCountRequest): Unit =
    mock.register(
      post(urlPathEqualTo(s"/organisations/employers/employee/counts"))
        .withRequestBody(equalToJson(Json.toJson(request).toString()))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(result).toString())))


  def searchCtReturnDetails(utr: String, result: CorporationTaxReturnDetailsResponse): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(result).toString())))

  def searchCtReturnDetailsNotFound(utr: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND).withBody("NO_DATA_FOUND")))

  def searchCtReturnDetailsNotFoundRateLimited(utr: String) =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.TOO_MANY_REQUESTS)))

  def searchSaDetails(utr: String, result: SelfAssessmentReturnDetailResponse): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(result).toString())))

  def searchSaDetailsNotFound(utr: String): Unit =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND).withBody("NO_DATA_FOUND")))

  def searchSaDetailsNotFoundRateLimited(utr: String) =
    mock.register(
      get(urlPathEqualTo(s"/organisations/self-assessment/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.TOO_MANY_REQUESTS)))


}
