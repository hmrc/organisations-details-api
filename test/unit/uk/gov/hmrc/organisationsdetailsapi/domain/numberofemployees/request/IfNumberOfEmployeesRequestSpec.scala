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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.request

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.numberofemployees.request.IfNumberOfEmployeesRequest

class IfNumberOfEmployeesRequestSpec extends AnyWordSpec with Matchers {
  "IfNumberOfEmployeesRequest writes to JSON successfully" in {
    val json =
      """
        |{
        |  "startDate": "2019-10-01",
        |  "endDate": "2020-04-05",
        |  "references": []
        |}
        |""".stripMargin

    val numberOfEmployeesRequest = IfNumberOfEmployeesRequest("2019-10-01", "2020-04-05", Seq.empty)

    val expectedResult = Json.parse(json)
    val result = Json.toJson(numberOfEmployeesRequest)

    result shouldBe expectedResult
  }
}
