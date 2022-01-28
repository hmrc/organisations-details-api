/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.Count
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeeCounts

class NumberOfEmployeeCountsSpec extends AnyWordSpec with Matchers {

  "Writes to json successfully" in {
    val expectedJson =
      """
        |{
        |   "numberOfEmployees": 1234,
        |   "dateOfCount": "2019-03"
        |}
        |""".stripMargin

    val selfAssessmentResponse = NumberOfEmployeeCounts(Some(1234), Some("2019-03"))

    val expectedResponse = Json.parse(expectedJson)

    val result = Json.toJson(selfAssessmentResponse)

    result shouldBe expectedResponse
  }

  "creates correctly from IF response" in {
    val count = Count(Some("2019-10"), Some(1234))

    val response = NumberOfEmployeeCounts.create(count)

    response.numberOfEmployees.get shouldBe 1234
    response.dateOfCount.get shouldBe "2019-10"
  }

}
