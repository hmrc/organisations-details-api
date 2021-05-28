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
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.IfAccountingPeriod

class IfAccountingPeriodSpec extends AnyWordSpec with Matchers {
  "AccountingPeriod reads from JSON successfully" in {
    val json =
      """
        |    {
        |      "apStartDate": "2018-04-06",
        |      "apEndDate": "2018-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val expectedResult = IfAccountingPeriod("2018-04-06", "2018-10-05", 38390)

    val result = Json.parse(json).validate[IfAccountingPeriod]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "AccountingPeriod reads from JSON unsuccessfully when startDate is incorrect" in {
    val json =
      """
        |    {
        |      "apStartDate": "20111-04-06",
        |      "apEndDate": "2018-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[IfAccountingPeriod]

    result.isSuccess shouldBe false
  }

  "AccountingPeriod reads from JSON unsuccessfully when endDate is incorrect" in {
    val json =
      """
        |    {
        |      "apStartDate": "2018-04-06",
        |      "apEndDate": "20111-10-05",
        |      "turnover": 38390
        |    }
        |""".stripMargin

    val result = Json.parse(json).validate[IfAccountingPeriod]

    result.isSuccess shouldBe false
  }
}
