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

package component.uk.gov.hmrc.organisationsdetailsapi.v1

import component.uk.gov.hmrc.organisationsdetailsapi.stubs.BaseSpec
import uk.gov.hmrc.organisationsdetailsapi.services.ScopesHelper

class IfQueriesSpec extends BaseSpec {

  val helper: ScopesHelper = app.injector.instanceOf[ScopesHelper]

  Feature("Query strings for 'corporation-tax' endpoint") {
    Scenario("For read:organisations-details-ho-suv") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-suv"), "corporation-tax")
      queryString mustBe "taxSolvencyStatus"
    }
  }

  Feature("Query strings for 'self-assessment' endpoint") {
    Scenario("For read:organisations-details-ho-suv") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-suv"), "self-assessment")
      queryString mustBe "taxSolvencyStatus"
    }
  }

  Feature("Query strings for 'number-of-employees' endpoint") {
    Scenario("For read:organisations-details-ho-suv") {
      val queryString = helper.getQueryStringFor(Seq("read:organisations-details-ho-suv"), "number-of-employees")
      queryString mustBe "references(counts(dateTaken,employeeCount),districtNumber,payeReference)"
    }
  }
}
