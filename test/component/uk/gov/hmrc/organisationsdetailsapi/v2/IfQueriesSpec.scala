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

package component.uk.gov.hmrc.organisationsdetailsapi.v2

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.BaseSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.organisationsdetailsapi.services.ScopesHelper

class IfQueriesSpec extends BaseSpec with Matchers {


  val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

  Feature("Query strings for 'corporation-tax' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "corporation-tax")
      queryString shouldBe "accountingPeriods(apEndDate,apStartDate,turnover),taxSolvencyStatus,taxpayerStartDate"
    }
  }

  Feature("Query strings for 'self-assessment' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "self-assessment")
      queryString shouldBe "startDate,taxSolvencyStatus,taxYears(businessSalesTurnover,taxyear)"
    }
  }

  Feature("Query strings for 'number-of-employees' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "number-of-employees")
      println(queryString)
      queryString shouldBe "references(counts(dateTaken,employeeCount),districtNumber,payeReference)"
    }
  }
}
