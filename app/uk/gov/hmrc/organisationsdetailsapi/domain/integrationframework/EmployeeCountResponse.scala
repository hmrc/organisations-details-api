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

import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{Format, JsPath}
import play.api.libs.json.Reads.pattern

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

import scala.util.matching.Regex


case class EmployeeCountResponse(startDate: String, endDate: String, references: Seq[PayeReferenceAndCount])

case class EmployeeCountRequest(startDate: String, endDate: String, references: Seq[PayeReference])

case class PayeReferenceAndCount(districtNumber: String, payeReference: String, counts: Seq[Count])

case class PayeReference(districtNumber: String, payeReference: String)


/*
employeeCount:
                      minimum: 1
                      maximum: 99999999
                      exclusiveMinimum: false
                      exclusiveMaximum: false
                      multipleOf: 1

                      dateTaken*	string
pattern: ^[1-2]{1}[0-9]{3}-[0-9]{2}$
 */
case class Count(dateTaken: String, employeeCount: Double)

object Count {

  val minValue = 1
  val maxValue = 99999999
  val datePattern: Regex = "^[1-2]{1}[0-9]{3}-[0-9]{2}$".r

  def isInRange(value: Double): Boolean =
    value >= minValue && value <= maxValue

  def isWholeNumber(value: Double): Boolean =
    BigDecimal(value) % 1 == 0

  def isInRangeAndWholeNumber(value: Double): Boolean =
    isInRange(value) && isWholeNumber(value)

  implicit val countFormat: Format[Count] = Format(
    (
      (JsPath \ "dateTaken").read[String](pattern(datePattern, "Date is in incorrect format")) and
      (JsPath \ "employeeCount").read[Double](verifying[Double](isInRangeAndWholeNumber))
    )(Count.apply _),
    (
      (JsPath \ "dateTaken").write[String] and
      (JsPath \ "employeeCount").write[Double]
    )(unlift(Count.unapply))
  )
}

object EmployeeCountResponse {

  val datePattern: Regex = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val districtPattern: Regex = "^[0-9]{3}$".r
  val payeRefPattern: Regex = "^[a-zA-Z0-9]{1,10}$".r

  implicit val referencesFormat: Format[PayeReferenceAndCount] = Format(
    (
      (JsPath \ "districtNumber").read[String](pattern(districtPattern, "District number is in the incorrect format")) and
      (JsPath \ "payeReference").read[String](pattern(payeRefPattern, "Paye reference is in the incorrect format")) and
      (JsPath \ "counts").read[Seq[Count]]
    )(PayeReferenceAndCount.apply _),
    (
      (JsPath \ "districtNumber").write[String] and
      (JsPath \ "payeReference").write[String] and
      (JsPath \ "counts").write[Seq[Count]]
    )(unlift(PayeReferenceAndCount.unapply))
  )

  implicit val ifEmployeeCountFormat: Format[EmployeeCountResponse] = Format(
    (
      (JsPath \ "startDate").read[String](pattern(datePattern, "startDate is in the incorrect format")) and
      (JsPath \ "endDate").read[String](pattern(datePattern, "endDate is in the incorrect format")) and
      (JsPath \ "references").read[Seq[PayeReferenceAndCount]]
    )(EmployeeCountResponse.apply _),
    (
      (JsPath \ "startDate").write[String] and
      (JsPath \ "endDate").write[String] and
      (JsPath \ "references").write[Seq[PayeReferenceAndCount]]
    )(unlift(EmployeeCountResponse.unapply))
  )
}

object EmployeeCountRequest {

  val datePattern: Regex = "^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$".r
  val districtPattern: Regex = "^[0-9]{3}$".r
  val payeRefPattern: Regex = "^[a-zA-Z0-9]{1,10}$".r

  implicit val referencesFormat: Format[PayeReference] = Format(
    (
      (JsPath \ "districtNumber").read[String](pattern(districtPattern, "District number is in the incorrect format")) and
      (JsPath \ "payeReference").read[String](pattern(payeRefPattern, "Paye reference is in the incorrect format"))
    )(PayeReference.apply _),
    (
      (JsPath \ "districtNumber").write[String] and
      (JsPath \ "payeReference").write[String]
    )(unlift(PayeReference.unapply))
  )

  implicit val ifEmployeeCountRequestFormat: Format[EmployeeCountRequest] = Format(
    (
      (JsPath \ "startDate").read[String](pattern(datePattern, "startDate is in the incorrect format")) and
      (JsPath \ "endDate").read[String](pattern(datePattern, "endDate is in the incorrect format")) and
      (JsPath \ "references").read[Seq[PayeReference]]
    )(EmployeeCountRequest.apply _),
    (
      (JsPath \ "startDate").write[String] and
      (JsPath \ "endDate").write[String] and
      (JsPath \ "references").write[Seq[PayeReference]]
    )(unlift(EmployeeCountRequest.unapply))
  )
}