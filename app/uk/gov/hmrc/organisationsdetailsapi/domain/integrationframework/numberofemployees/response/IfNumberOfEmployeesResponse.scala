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

case class IfNumberOfEmployeesResponse(startDate: String, endDate: String, references: Seq[IfNumberOfEmployeeReferencesResponse])

object IfNumberOfEmployeesResponse {

  private val datePattern = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r

  implicit val numberOfEmployeesResponseFormat: Reads[IfNumberOfEmployeesResponse] = (
      (JsPath \ "startDate").read[String](pattern(datePattern, "startDate is invalid")) and
        (JsPath \ "endDate").read[String](pattern(datePattern, "endDate is invalid")) and
        (JsPath \ "references").read[Seq[IfNumberOfEmployeeReferencesResponse]]
      )(IfNumberOfEmployeesResponse.apply _)
}