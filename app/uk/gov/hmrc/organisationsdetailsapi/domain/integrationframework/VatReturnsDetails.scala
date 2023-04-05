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

package uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework


import play.api.libs.json.{Format, JsPath, JsResult, JsValue, Reads}
import scala.util.matching.Regex
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._


//make all optional, excluding fields for scope etc



object VatReturnDetails{

  case class VatReturn(calendarMonth: Option[Int], liabilityMonth: Option[Int], numMonthsAssessed: Option[Int], box6Total: Option[Int], returnType: Option[String], source: Option[String])

  case class TaxYears(taxYear: Option[String], vatReturns: Option[Seq[VatReturn]])

  case class VatReturnDetailsResponse(vrn: Option[String], appDate: Option[String], taxYears: Option[Seq[TaxYears]])

  implicit val vatReturn: Reads[VatReturn] =
    (
      (JsPath \ "calendarMonth").readNullable[Int] and
        (JsPath \ "liabilityMonth").readNullable[Int] and
        (JsPath \ "numMonthsAssessed").readNullable[Int] and
        (JsPath \"box6Total").readNullable[Int] and
        (JsPath \ "returnType").readNullable[String] and
        (JsPath \ "source").readNullable[String]
    )(VatReturn.apply _)

  implicit val taxYears: Reads[TaxYears] =
  (
      (JsPath \ "taxYear").readNullable[String] and
        (JsPath \ "vatReturns").readNullable[Seq[VatReturn]]
  )(TaxYears.apply _)

  implicit val vatReturnDetailsResponseFormat: Reads[VatReturnDetailsResponse]=
    (
      (JsPath \ "vrn").readNullable[String] and
        (JsPath \ "appDate").readNullable[String] and
        (JsPath \ "taxYears").readNullable[Seq[TaxYears]]
      )(VatReturnDetailsResponse.apply _)
}
