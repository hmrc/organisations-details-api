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

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetailsResponse
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._

object IfStub extends MockHost(8443) {

  def searchCtReturnDetails(utr: String, result: CorporationTaxReturnDetailsResponse): StubMapping =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.OK).withBody(Json.toJson(result).toString())))

  def searchCtReturnDetailsNotFound(utr: String): StubMapping =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.NOT_FOUND).withBody("NO_DATA_FOUND")))

  def searchCtReturnDetailsNotFoundRateLimited(utr: String) =
    mock.register(
      get(urlPathEqualTo(s"/organisations/corporation-tax/$utr/return/details"))
        .willReturn(aResponse().withStatus(Status.TOO_MANY_REQUESTS)))

}
