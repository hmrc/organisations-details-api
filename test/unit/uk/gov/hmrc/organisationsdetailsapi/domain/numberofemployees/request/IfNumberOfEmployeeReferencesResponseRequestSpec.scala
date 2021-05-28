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
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.numberofemployees.request.IfNumberOfEmployeeReferencesRequest

class IfNumberOfEmployeeReferencesResponseRequestSpec extends AnyWordSpec with Matchers {
  "IfNumberOfEmployeeReferencesRequest writes to JSON successfully" in {
    val json =
      """
        |{
        |  "districtNumber": "456",
        |  "payeReference": "1234567890"
        |}
        |""".stripMargin

    val numberOfEmployeeReferencesRequest = IfNumberOfEmployeeReferencesRequest("456", "1234567890")

    val result = Json.toJson(numberOfEmployeeReferencesRequest)
    val expectedResult = Json.parse(json)

    result shouldBe expectedResult
  }
}
