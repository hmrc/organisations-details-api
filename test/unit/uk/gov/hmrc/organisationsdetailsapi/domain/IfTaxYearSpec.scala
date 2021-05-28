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
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.IfTaxYear

class IfTaxYearSpec extends AnyWordSpec with Matchers {
  "TaxYear read from JSON successfully" in {
    val json =
      """
        |{
        |   "taxYear": "2019",
        |   "businessSalesTurnover": 12343.12
        |}
        |""".stripMargin

    val expectedResult = IfTaxYear("2019", 12343.12)

    val result = Json.parse(json).validate[IfTaxYear]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "TaxYear read from JSON unsuccessfully when taxYear is invalid" in {
    val json =
      """
        |{
        |   "taxYear": "4019",
        |   "businessSalesTurnover": 12343.12
        |}
        |""".stripMargin

    val result = Json.parse(json).validate[IfTaxYear]

    result.isSuccess shouldBe false
  }
}
