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

package uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Writes}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetailsResponse

import java.time.LocalDate

case class CorporationTaxResponse(dateOfRegistration: Option[LocalDate], taxSolvencyStatus: Option[String], accountingPeriods: Option[Seq[AccountingPeriod]])

object CorporationTaxResponse {

  def create(corporationTaxReturnDetailsResponse: CorporationTaxReturnDetailsResponse): CorporationTaxResponse =
    CorporationTaxResponse(
      corporationTaxReturnDetailsResponse.taxpayerStartDate.map(LocalDate.parse) ,
      corporationTaxReturnDetailsResponse.taxSolvencyStatus,
      corporationTaxReturnDetailsResponse.accountingPeriods.map(x => x.map(AccountingPeriod.create))
    )

  implicit val corporationTaxResponseWrites : Writes[CorporationTaxResponse] = (
    (JsPath \ "dateOfRegistration").writeNullable[LocalDate] and
      (JsPath \ "taxSolvencyStatus").writeNullable[String] and
      (JsPath \ "accountingPeriods").writeNullable[Seq[AccountingPeriod]]
  )(unlift(CorporationTaxResponse.unapply))
}
