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
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.numberofemployees.response.IfNumberOfEmployeesResponse

class IfNumberOfEmployeesResponseSpec extends AnyWordSpec with Matchers{
  "IfNumberOfEmployeesResponse reads from JSON successfully" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val expectedResult = IfNumberOfEmployeesResponse("2019-10-01", "2020-04-05", Seq.empty)

    val result = Json.parse(json).validate[IfNumberOfEmployeesResponse]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "IfNumberOfEmployeesResponse reads from JSON unsuccessfully when startDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "20111-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfNumberOfEmployeesResponse]

    result.isSuccess shouldBe false
  }

  "IfNumberOfEmployeesResponse reads from JSON unsuccessfully when endDate is incorrect" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "20222-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfNumberOfEmployeesResponse]

    result.isSuccess shouldBe false
  }
}
