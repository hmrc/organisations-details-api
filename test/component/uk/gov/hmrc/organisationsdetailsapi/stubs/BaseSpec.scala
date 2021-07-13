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
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames.{ACCEPT, AUTHORIZATION, CONTENT_TYPE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.MimeTypes.JSON

import scala.concurrent.duration.Duration

trait BaseSpec
  extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with GuiceOneServerPerSuite
    with GivenWhenThen {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled"                               -> false,
      "auditing.traceRequests"                                 -> false,
      "run.mode"                                               -> "It"
    )
    .build()

  val timeout = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://localhost:$port"
  val mocks = Seq()
  val authToken = "Bearer AUTH_TOKEN"
  val acceptHeaderVP1 = ACCEPT -> "application/vnd.hmrc.P1.0+json"
  val sampleCorrelationId = "188e9400-b636-4a3b-80ba-230a8c72b92a"
  val validCorrelationHeader = ("CorrelationId", sampleCorrelationId)

  protected def requestHeaders(
                                acceptHeader: (String, String) = acceptHeaderVP1,
                                correlationHeader: (String, String) = validCorrelationHeader) =
    Map(CONTENT_TYPE -> JSON, AUTHORIZATION -> authToken, acceptHeader, correlationHeader)
}
