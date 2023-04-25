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

package component.uk.gov.hmrc.organisationsdetailsapi.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{ aResponse, get, okJson, urlEqualTo }
import play.api.libs.json.Json

import java.util.UUID

object OrganisationsMatchingApiStub extends MockHost(9657) {

  def willRespondWith(matchId: String, responseCode: Int, responseBody: String = ""): Unit =
    mock.register(
      get(urlEqualTo(s"/match-record/$matchId"))
        .willReturn(aResponse().withStatus(responseCode).withBody(responseBody)))

  def hasMatchingRecord(matchId: String, utr: String): Unit =
    mock.register(
      get(urlEqualTo(s"/match-record/$matchId"))
        .willReturn(okJson(Json.obj("matchId" -> matchId, "utr" -> utr).toString)))

  def hasMatchingVatRecord(matchId: UUID, vrn: String): Unit =
    mock.register(
      get(urlEqualTo(s"/match-record/vat/$matchId"))
        .willReturn(okJson(Json.obj("matchId" -> matchId.toString, "vrn" -> vrn).toString))
    )
}
