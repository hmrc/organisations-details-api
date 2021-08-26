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

package component.uk.gov.hmrc.organisationsdetailsapi.v1

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.BaseSpec
import uk.gov.hmrc.organisationsdetailsapi.services.ScopesHelper

class IfQueriesSpec extends BaseSpec {

  val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

  Feature("Query strings for 'corporation-tax' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "corporation-tax")
      queryString mustBe "accountingPeriods(apEndDate,apStartDate,turnover),taxSolvencyStatus,taxpayerStartDate"
    }
  }

  Feature("Query strings for 'self-assessment' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "self-assessment")
      queryString mustBe "startDate,taxSolvencyStatus,taxyears(businessSalesTurnover,taxyear)"
    }
  }

  Feature("Query strings for 'number-of-employees' endpoint") {
    Scenario("For read:organisations-details-ho-ssp") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-ssp"), "number-of-employees")
      queryString mustBe "references(counts(dateTaken,employeeCount),districtNumber,payeReference)"
    }
  }
}
