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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.PayeReferenceAndCount
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeesResponse

class NumberOfEmployeesResponseSpec extends AnyWordSpec with Matchers {
  "Writes to json successfully" in {
    val expectedJson =
      """
        |{
        |  "payeReference": "456/RT882d",
        |  "counts": []
        |}
        |""".stripMargin

    val numberOfEmployeesResponse = NumberOfEmployeesResponse(Some("456/RT882d"), Some(Seq.empty))

    val expectedResponse = Json.parse(expectedJson)

    val result = Json.toJson(numberOfEmployeesResponse)

    result shouldBe expectedResponse
  }

  "creates correctly from IF number of employee count" in {

    val payeReferenceAndCount =  PayeReferenceAndCount(
      Some("123"),
      Some("RT882d"),
      None
    )

    val result = NumberOfEmployeesResponse.create(payeReferenceAndCount)

    result.payeReference.get shouldBe "123/RT882d"
    result.counts shouldBe None
  }
}
