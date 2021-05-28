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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.IfCorporationTaxReturnDetails

class IfCorporationTaxReturnDetailsSpec extends AnyWordSpec with Matchers {
  "IfCorporationTaxReturnDetails reads from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val expectedResult = IfCorporationTaxReturnDetails("1234567890", "2015-04-21", "V", Seq.empty)

    val result = Json.parse(json).validate[IfCorporationTaxReturnDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "IfCorporationTaxReturnDetails reads from JSON unsuccessfully when utr is incorrect" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfCorporationTaxReturnDetails]

    result.isSuccess shouldBe false
  }

  "IfCorporationTaxReturnDetails reads from JSON unsuccessfully when taxpayerStartDate is incorrect" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "taxpayerStartDate": "20111-04-21",
        |  "taxSolvencyStatus": "V",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfCorporationTaxReturnDetails]

    result.isSuccess shouldBe false
  }

  "IfCorporationTaxReturnDetails reads from JSON unsuccessfully when tax solvency status is not one of V, S, I, A" in {
    val json =
      """
        |{
        |  "utr": "123456789A",
        |  "taxpayerStartDate": "2015-04-21",
        |  "taxSolvencyStatus": "X",
        |  "accountingPeriods": []
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfCorporationTaxReturnDetails]

    result.isSuccess shouldBe false
  }
}
