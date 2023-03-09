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

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec
  extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with GuiceOneServerPerSuite
    with GivenWhenThen {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "mongodb.uri" -> "mongodb://127.0.0.1:27017/organisations-details-api",
      "microservice.services.integration-framework.host" -> "127.0.0.1",
      "auditing.enabled" -> false,
      "auditing.traceRequests" -> false,
      "microservice.services.auth.port" -> AuthStub.port,
      "microservice.services.organisations-matching-api.port" -> OrganisationsMatchingApiStub.port,
      "microservice.services.integration-framework.port" -> IfStub.port,
      "run.mode" -> "It"
    )
    .build()

  val timeout: FiniteDuration = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://127.0.0.1:$port"
  val mocks = Seq(AuthStub, IfStub, OrganisationsMatchingApiStub)
  val authToken = "Bearer AUTH_TOKEN"
  val acceptHeaderVP1: (String, String) = ACCEPT -> "application/vnd.hmrc.1.0+json"
  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val correlationIdHeaderMalformed: (String, String) = "CorrelationId" -> "foo"

  val validCorrelationHeader: (String, String) = ("CorrelationId", sampleCorrelationId)

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


  override protected def beforeEach(): Unit = {
    mocks.foreach(m => if (!m.server.isRunning) m.server.start())
  }

  override protected def afterEach(): Unit =
    mocks.foreach(_.mock.resetMappings())

  override def afterAll(): Unit = {
    mocks.foreach(_.server.stop())
  }
}

case class MockHost(port: Int) {
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  val mock = new WireMock("localhost", port)
  val url = s"http://127;0.0.1:9000"
}