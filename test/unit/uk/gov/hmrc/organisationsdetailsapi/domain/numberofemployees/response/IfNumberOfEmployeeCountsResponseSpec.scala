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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.response

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.numberofemployees.response.IfNumberOfEmployeeCountsResponse

class IfNumberOfEmployeeCountsResponseSpec extends AnyWordSpec with Matchers {
  "IfNumberOfEmployeeCountsResponse reads from JSON successfully" in {
    val json =
      """
        |{
        |    "dateTaken": "2019-10",
        |    "employeeCount": 554
        |}
        |""".stripMargin

    val expectedResult = IfNumberOfEmployeeCountsResponse("2019-10", 554)

    val result = Json.parse(json).validate[IfNumberOfEmployeeCountsResponse]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "IfNumberOfEmployeeCountsResponse reads from JSON unsuccessfully when dateTake is invalid" in {
    val json =
      """
        |{
        |    "dateTaken": "20191-10",
        |    "employeeCount": 554
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfNumberOfEmployeeCountsResponse]

    result.isSuccess shouldBe false
  }
}
