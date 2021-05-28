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
import play.api.libs.json.Reads.pattern
import play.api.libs.json.{JsPath, Reads}

case class IfTaxYear(taxYear: String, businessSalesTurnover: Double)

object IfTaxYear {

  private val taxYearPattern = "^20[0-9]{2}$".r

  implicit val taxYearFormat: Reads[IfTaxYear] = (
      (JsPath \ "taxYear").read[String](pattern(taxYearPattern, "Tax Year is in the incorrect Format")) and
        (JsPath \ "businessSalesTurnover").read[Double]
      )(IfTaxYear.apply _)
}