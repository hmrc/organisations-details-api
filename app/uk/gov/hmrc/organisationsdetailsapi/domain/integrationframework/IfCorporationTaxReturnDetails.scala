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
import play.api.libs.json.Reads.{pattern, verifying}
import play.api.libs.json.{JsPath, Reads}

case class IfCorporationTaxReturnDetails(utr: String, taxpayerStartDate: String, taxSolvencyStatus: String, accountingPeriods: Seq[IfAccountingPeriod])

object IfCorporationTaxReturnDetails {

  private val utrPattern = "^[0-9]{10}$".r
  private val taxpayerStartDatePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  private def validTaxSolvencyStatus(value: String) = Seq("V", "S", "I", "A").contains(value)

  implicit val corporationTaxReturnDetailsResponseFormat: Reads[IfCorporationTaxReturnDetails] = (
    (JsPath \ "utr").read[String](pattern(utrPattern, "Invalid UTR format")) and
      (JsPath \ "taxpayerStartDate").read[String](pattern(taxpayerStartDatePattern, "Invalid taxpayer start date")) and
      (JsPath \ "taxSolvencyStatus").read[String](verifying(validTaxSolvencyStatus)) and
      (JsPath \ "accountingPeriods").read[Seq[IfAccountingPeriod]]
    ) (IfCorporationTaxReturnDetails.apply _)
}