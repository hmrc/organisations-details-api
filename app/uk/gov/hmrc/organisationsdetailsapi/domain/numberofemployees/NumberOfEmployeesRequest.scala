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

package uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees
import play.api.libs.json.{Format, JsPath, Json}

import scala.util.matching.Regex

case class PayeReference(districtNumber: String, schemeReference: String);

case class NumberOfEmployeesRequest(fromDate: String, toDate: String, payeReference: Seq[PayeReference])

object NumberOfEmployeesRequest {
  val datePattern: Regex =
    "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val districtPattern: Regex = "^[0-9]{3}$".r
  val schemeRefPattern: Regex = "^[a-zA-Z0-9]{1,10}$".r

  implicit val referencesReads: Format[PayeReference] = Json.format

  implicit val numberOfEmployeesRequestReads: Format[NumberOfEmployeesRequest] = Json.format
}
