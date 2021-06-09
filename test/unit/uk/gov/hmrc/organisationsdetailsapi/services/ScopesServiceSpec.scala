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

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.organisationsdetailsapi.services.ScopesService
import utils.TestSupport

class ScopesServiceSpec extends TestSupport with ScopesConfig with BeforeAndAfterEach {

  "Scopes service" should {

    val scopesService = new ScopesService(mockConfig)

    "map multiple items correctly" in {
      val result = scopesService.getScopeItems(mockScope1)
      result.head shouldBe "payments"
      result(1) shouldBe "employer/employerName"
      result(2) shouldBe "employer/employerDistrictNumber"
    }

    "return empty list if no items found" in {
      val result = scopesService.getScopeItems("test-scope")
      result shouldBe List()
    }

    "get data items for endpoint" in {
      val result = scopesService.getEndpointFieldKeys(mockEndpoint1)
      result shouldBe List("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
    }

    "return empty list if endpoint not found" in {
      val result = scopesService.getEndpointFieldKeys("test-scope")
      result shouldBe List()
    }

    "get valid data items for scope and endpoint" in {
      val result =
        scopesService.getValidItemsFor(List(mockScope1), mockEndpoint1)
      result shouldBe List("payments", "employer/employerName", "employer/employerDistrictNumber")
    }

    "get valid data items for scope and multiple endpoints" in {
      val result =
        scopesService.getValidItemsFor(List(mockScope3), List(mockEndpoint1, mockEndpoint3))
      result shouldBe Set(
        "payments",
        "field4"
      )
    }

    "get valid data items keys for single scope" in {
      val result =
        scopesService.getValidFieldsForCacheKey(List(mockScope1))
      result shouldBe "ABF"
    }

    "get valid data items keys for multiple scopes" in {
      val result =
        scopesService.getValidFieldsForCacheKey(List(mockScope1, mockScope2))
      result shouldBe "ABFCDEG"
    }

    "get valid data items keys for multiple scopes including no match" in {
      val result =
        scopesService.getValidFieldsForCacheKey(List(mockScope1, mockScope2, "not-exists"))
      result shouldBe "ABFCDEG"
    }

    "identity accesssible endpoints" in {
      val result = scopesService.getAccessibleEndpoints(List(mockScope3)).toList
      result.contains(mockEndpoint1) shouldBe true
      result.contains(mockEndpoint2) shouldBe false
      result.contains(mockEndpoint3) shouldBe true
    }

    "get links for valid endpoints" in {
      val result = scopesService.getEndpoints(List(mockScope1))
      result.size shouldBe 1
      val config1 = result.head
      config1.name shouldBe mockEndpoint1
      config1.link shouldBe "/a/b/c?matchId=<matchId>{&fromDate,toDate}"

      val result2 = scopesService.getEndpoints(List(mockScope4))
      val config2 = result2.head
      config2.name shouldBe mockEndpoint2
      config2.link shouldBe "/a/b/d?matchId=<matchId>{&fromDate,toDate}"
    }

    "get the scopes associated to an endpoint" in {
      val result = scopesService.getEndPointScopes(mockEndpoint2)
      result shouldBe Iterable(mockScope4, mockScope6, mockScope7)
    }
  }
}
