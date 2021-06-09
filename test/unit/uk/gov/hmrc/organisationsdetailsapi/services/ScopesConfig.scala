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
  val mockScope3 = "test3"
  val mockScope4 = "test4"
  val mockScope5 = "test5"
  val mockScope6 = "test6"
  val mockScope7 = "test7"
  val mockScope8 = "test8"

  val mockEndpoint1 = "endpoint1"
  val mockEndpoint2 = "endpoint2"
  val mockEndpoint3 = "endpoint3"
  val mockEndpoint4 = "endpoint4"

  val mockConfig = Configuration(
    (s"api-config.scopes.$mockScope1.fields", List("A", "B", "F")),
    (s"api-config.scopes.$mockScope2.fields", List("A", "B", "C", "D", "E", "F", "G")),
    (s"api-config.scopes.$mockScope3.fields", List("A", "P")),
    (s"api-config.scopes.$mockScope4.fields", List("AC", "N")),
    (s"api-config.scopes.$mockScope5.fields", List("O", "P")),
    (s"api-config.scopes.$mockScope6.fields", List("Q", "R")),
    (s"api-config.scopes.$mockScope7.fields", List("Q", "R", "S", "T")),
    (s"api-config.endpoints.$mockEndpoint1.endpoint", "/a/b/c?matchId=<matchId>{&fromDate,toDate}"),
    (s"api-config.endpoints.$mockEndpoint1.title", "title"),
    (s"api-config.endpoints.$mockEndpoint1.fields.A", "payments"),
    (s"api-config.endpoints.$mockEndpoint1.fields.B", "employer/employerName"),
    (s"api-config.endpoints.$mockEndpoint1.fields.C", "employer/employerAddress/line1"),
    (s"api-config.endpoints.$mockEndpoint1.fields.D", "employer/employerAddress/line2"),
    (s"api-config.endpoints.$mockEndpoint1.fields.E", "employer/employerAddress/line3"),
    (s"api-config.endpoints.$mockEndpoint1.fields.F", "employer/employerDistrictNumber"),
    (s"api-config.endpoints.$mockEndpoint1.fields.G", "employer/employerSchemeReference"),
    (s"api-config.endpoints.$mockEndpoint1.fields.H", "employmentStartDate"),
    (s"api-config.endpoints.$mockEndpoint1.fields.I", "employmentLeavingDate"),
    (s"api-config.endpoints.$mockEndpoint1.fields.J", "employmentPayFrequency"),
    (s"api-config.endpoints.$mockEndpoint1.fields.K", "employeeAddress"),
    (s"api-config.endpoints.$mockEndpoint1.fields.L", "payrollId"),
    (s"api-config.endpoints.$mockEndpoint2.endpoint", "/a/b/d?matchId=<matchId>{&fromDate,toDate}"),
    (s"api-config.endpoints.$mockEndpoint2.title", "title"),
    (s"api-config.endpoints.$mockEndpoint2.fields.AC", "field1"),
    (s"api-config.endpoints.$mockEndpoint2.fields.N", "field2"),
    (s"api-config.endpoints.$mockEndpoint2.fields.Q", "field2"),
    (s"api-config.endpoints.$mockEndpoint2.fields.R", "field2"),
    (s"api-config.endpoints.$mockEndpoint3.endpoint", "/a/b/e?matchId=<matchId>{&fromDate,toDate}"),
    (s"api-config.endpoints.$mockEndpoint3.title", "title"),
    (s"api-config.endpoints.$mockEndpoint3.fields.O", "field3"),
    (s"api-config.endpoints.$mockEndpoint3.fields.P", "field4"),

    (s"api-config.scopes.$mockScope8.fields", List("M", "AD", "S", "T", "U", "V", "W")),
    (s"api-config.scopes.$mockScope8.filters", List("M")),
    (s"api-config.endpoints.$mockEndpoint4.endpoint", "/a/b/c?matchId=<matchId>{&fromDate,toDate}"),
    (s"api-config.endpoints.$mockEndpoint4.title", "title"),
    (s"api-config.endpoints.$mockEndpoint4.fields.M", "payments"),
    (s"api-config.endpoints.$mockEndpoint4.fields.AD", "employer/employerName"),
    (s"api-config.endpoints.$mockEndpoint4.fields.S", "employer/employerAddress/line1"),
    (s"api-config.endpoints.$mockEndpoint4.fields.T", "employer/employerAddress/line2"),
    (s"api-config.endpoints.$mockEndpoint4.fields.U", "employer/employerAddress/line3"),
    (s"api-config.endpoints.$mockEndpoint4.fields.V", "employer/employerDistrictNumber"),
    (s"api-config.endpoints.$mockEndpoint4.fields.W", "employer/employerSchemeReference"),
    (s"api-config.endpoints.$mockEndpoint4.fields.X", "employmentStartDate"),
    (s"api-config.endpoints.$mockEndpoint4.fields.Y", "employmentLeavingDate"),
    (s"api-config.endpoints.$mockEndpoint4.fields.Z", "employmentPayFrequency"),
    (s"api-config.endpoints.$mockEndpoint4.fields.AA", "employeeAddress"),
    (s"api-config.endpoints.$mockEndpoint4.fields.AB", "payrollId"),
    (s"api-config.endpoints.$mockEndpoint4.filters.M", "employerRef eq '<employerRef>'")

  )

  val mockApiConfig = ApiConfig(
    scopes = List(
      ScopeConfig(mockScope1, List("A", "B", "F")),
      ScopeConfig(mockScope2, List("A", "B", "C", "D", "E", "F", "G")),
      ScopeConfig(mockScope3, List("A", "P")),
      ScopeConfig(mockScope4, List("AC", "N")),
      ScopeConfig(mockScope5, List("O", "P")),
      ScopeConfig(mockScope8, List("M", "AD", "S", "T", "U", "V", "W"), filters = List("M"))
    ),
    endpoints = List(
      EndpointConfig(
        name = mockEndpoint1,
        title = "title",
        link = "/a/b/c?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "A" -> "payments",
          "B" -> "employer/employerName",
          "C" -> "employer/employerAddress/line1",
          "D" -> "employer/employerAddress/line2",
          "E" -> "employer/employerAddress/line3",
          "F" -> "employer/employerDistrictNumber",
          "G" -> "employer/employerSchemeReference",
          "H" -> "employmentStartDate",
          "I" -> "employmentLeavingDate",
          "J" -> "employmentPayFrequency",
          "K" -> "employeeAddress",
          "L" -> "payrollId"
        )
      ),
      EndpointConfig(
        name = mockEndpoint2,
        title = "title",
        link = "/a/b/d?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "AC" -> "field1",
          "N" -> "field2"
        )),
      EndpointConfig(
        name = mockEndpoint3,
        title = "title",
        link = "/a/b/e?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "O" -> "field3",
          "P" -> "field4"
        )),
      EndpointConfig(
        name = mockEndpoint4,
        title = "title",
        link = "/a/b/c?matchId=<matchId>{&fromDate,toDate}",
        fields = Map(
          "M" -> "payments",
          "AD" -> "employer/employerName",
          "S" -> "employer/employerAddress/line1",
          "T" -> "employer/employerAddress/line2",
          "U" -> "employer/employerAddress/line3",
          "V" -> "employer/employerDistrictNumber",
          "W" -> "employer/employerSchemeReference",
          "X" -> "employmentStartDate",
          "Y" -> "employmentLeavingDate",
          "Z" -> "employmentPayFrequency",
          "AA" -> "employeeAddress",
          "AB" -> "payrollId"
        ),
        filters = Map(
          "M" -> "employerRef eq '<employerRef>'"
        )
      )
    )
  )
}

