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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.AccountingPeriod
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{AccountingPeriod => IfAccountingPeriod}

import java.time.LocalDate

class AccountingPeriodSpec extends AnyWordSpec with Matchers {

  "Writes to Json Successfully" in {
    val json =
      """
        |{
        |   "accountingPeriodStartDate" : "2018-04-06",
        |   "accountingPeriodEndDate" : "2018-10-05",
        |   "turnover" : 38390
        |}
        |""".stripMargin

    val accountingPeriod = AccountingPeriod(
      Some(LocalDate.parse("2018-04-06")),
      Some(LocalDate.parse("2018-10-05")),
      Some(38390)
    )
    val expectedResult = Json.parse(json)

    val result = Json.toJson(accountingPeriod)

    result shouldBe expectedResult
  }

  "Create from a full IF Accounting Period" in {
    val ifAccountingPeriod = IfAccountingPeriod(
      Some("2018-04-06"),
      Some("2018-10-05"),
      Some(38390)
    )

    val result = AccountingPeriod.create(ifAccountingPeriod)

    result.accountingPeriodStartDate.get shouldBe LocalDate.of(2018, 4, 6)
    result.accountingPeriodEndDate.get shouldBe LocalDate.of(2018, 10, 5)
    result.turnover.get shouldBe 38390
  }

}
