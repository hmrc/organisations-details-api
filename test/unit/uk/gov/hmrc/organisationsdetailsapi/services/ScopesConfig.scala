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

package unit.uk.gov.hmrc.organisationsdetailsapi.services

import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.organisationsdetailsapi.config.{ApiConfig, EndpointConfig, ScopeConfig}

trait ScopesConfig extends MockitoSugar {

  val mockScope1 = "test1"
  val mockScope2 = "test2"

  val mockEndpoint1 = "endpoint1"
  val mockEndpoint2 = "endpoint2"

  val mockConfig = Configuration(
    (s"api-config.scopes.$mockScope1.fields", List("A", "B", "D")),
    (s"api-config.scopes.$mockScope2.fields", List("A", "B", "C", "D")),
    (s"api-config.endpoints.$mockEndpoint1.endpoint", "/a/b/c?matchId=<matchId>{&fromDate,toDate}"),
    (s"api-config.endpoints.$mockEndpoint1.title", "title"),
    (s"api-config.endpoints.$mockEndpoint1.fields.A", "field1"),
    (s"api-config.endpoints.$mockEndpoint1.fields.B", "field2/subfield1"),
    (s"api-config.endpoints.$mockEndpoint1.fields.C", "field2/subfield2"),
    (s"api-config.endpoints.$mockEndpoint1.fields.D", "field3"),

  )

  val mockApiConfig = ApiConfig(
    scopes = List(
      ScopeConfig(mockScope1, List("A", "B", "D")),
      ScopeConfig(mockScope2, List("A", "B", "C", "D"))
    ),
    endpoints = List(
      EndpointConfig(
        name = mockEndpoint1,
        title = "title",
        link = "/a/b/c?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "A" -> "field1",
          "B" -> "field2/subfield1",
          "C" -> "field2/subfield2",
          "D" -> "field3"
        )
      ),
      EndpointConfig(
        name = mockEndpoint2,
        title = "title",
        link = "/a/b/d?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "E" -> "field4",
          "F" -> "field5"
        ))
      )
  )
}

