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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.paye

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.paye.PayeResponse

import java.time.LocalDate

class PayeResponseSpec extends AnyWordSpec with Matchers {
  "Writes to json successfully" in {
    val json =
      """
        |{
        |   "dateOfRegistration": "2015-04-21",
        |   "taxSolvencyStatus" : "V",
        |   "accountingPeriods" : []
        |}
        |""".stripMargin

    val payeResponse = PayeResponse(LocalDate.parse("2015-04-21"), "V", Seq.empty)

    val expectedResult = Json.parse(json)
    val result = Json.toJson(payeResponse)

    result shouldBe expectedResult
  }
}
