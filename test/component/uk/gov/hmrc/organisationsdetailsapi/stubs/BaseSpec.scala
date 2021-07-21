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

import java.util.concurrent.TimeUnit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.MimeTypes.JSON

import scala.concurrent.duration.Duration

trait BaseSpec
  extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with GuiceOneServerPerSuite
    with GivenWhenThen {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled"                               -> false,
      "auditing.traceRequests"                                 -> false,
      "microservice.services.organisations-matching-api.port"    -> OrganisationsMatchingApiStub.port,
      "microservice.services.integration-framework.port"       -> IfStub.port,
      "run.mode"                                               -> "It"
    )
    .build()

  val timeout = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://localhost:$port"
  val mocks = Seq(AuthStub, IfStub, OrganisationsMatchingApiStub)
  val authToken = "Bearer AUTH_TOKEN"
  val acceptHeaderVP1 = ACCEPT -> "application/vnd.hmrc.P1.0+json"
  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val correlationIdHeaderMalformed = "CorrelationId" -> "foo"

  val validCorrelationHeader = ("CorrelationId", sampleCorrelationId)

  protected def requestHeaders(
                                acceptHeader: (String, String) = acceptHeaderVP1,
                                correlationHeader: (String, String) = validCorrelationHeader) =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationHeader)


  protected def requestHeadersInvalid(acceptHeader: (String, String) = acceptHeaderVP1) =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader)

  protected def requestHeadersMalformed(acceptHeader: (String, String) = acceptHeaderVP1) =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationIdHeaderMalformed)

  protected def invalidRequest(message: String) =
    s"""{"code":"INVALID_REQUEST","message":"$message"}"""


}

case class MockHost(port: Int) {
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  val mock = new WireMock("localhost", port)
  val url = s"http://localhost:9000"
}