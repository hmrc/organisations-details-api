/*
 * Copyright 2023 HM Revenue & Customs
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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.CorporationTaxResponse
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetailsResponse

import java.time.LocalDate

class CorporationTaxResponseSpec extends AnyWordSpec with Matchers {
  "Writes to json successfully" in {
    val json =
      """
        |{
        |   "dateOfRegistration": "2015-04-21",
        |   "taxSolvencyStatus" : "V",
        |   "accountingPeriods" : []
        |}
        |""".stripMargin

    val corporationTaxResponse = CorporationTaxResponse(
      Some(LocalDate.parse("2015-04-21")),
      Some("V"),
      Some(Seq.empty))

    val expectedResult = Json.parse(json)
    val result = Json.toJson(corporationTaxResponse)

    result shouldBe expectedResult
  }

  "Create from a full IF Corporation Tax response" in {
    val corporationTaxReturnDetailsResponse = CorporationTaxReturnDetailsResponse(
      Some("1234567890"),
      Some("2015-04-21"),
      Some("V"),
      None
    )

    val result = CorporationTaxResponse.create(corporationTaxReturnDetailsResponse)

    result.taxSolvencyStatus.get shouldBe "V"
    result.dateOfRegistration.get shouldBe LocalDate.of(2015, 4, 21)

  }
}
