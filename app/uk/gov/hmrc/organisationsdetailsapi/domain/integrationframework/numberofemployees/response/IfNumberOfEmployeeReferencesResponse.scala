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

package uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.numberofemployees.response

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads.pattern
import play.api.libs.json.{JsPath, Reads}

case class IfNumberOfEmployeeReferencesResponse(districtNumber: String, payeReference: String, counts: Seq[IfNumberOfEmployeeCountsResponse])

object IfNumberOfEmployeeReferencesResponse {

  private val districtNumberPattern = "^[0-9]{3}$".r
  private val payeRefPattern = "^[a-zA-Z0-9]{1,10}$".r

  implicit val numberOfEmployeeReferencesFormat: Reads[IfNumberOfEmployeeReferencesResponse] = (
      (JsPath \ "districtNumber").read[String](pattern(districtNumberPattern, "District number is invalid")) and
        (JsPath \ "payeReference").read[String](pattern(payeRefPattern, "payeReference is invalid")) and
        (JsPath \ "counts").read[Seq[IfNumberOfEmployeeCountsResponse]]
      )(IfNumberOfEmployeeReferencesResponse.apply _)
}
