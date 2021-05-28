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

package uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.json.Reads._
import play.api.libs.json.Reads.pattern

case class IfAccountingPeriod(apStartDate: String, apEndDate: String, turnover: Int)

object IfAccountingPeriod {
  private val apDatePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  implicit val accountingPeriodReads: Reads[IfAccountingPeriod] = (
    (JsPath \ "apStartDate").read[String](pattern(apDatePattern, "apStartDate not in correct format")) and
        (JsPath \ "apEndDate").read[String](pattern(apDatePattern, "apEndDate not in correct format")) and
        (JsPath \ "turnover").read[Int]
      )(IfAccountingPeriod.apply _)
}