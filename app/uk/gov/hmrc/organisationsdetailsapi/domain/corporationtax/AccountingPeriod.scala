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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{AccountingPeriod => IfAccountingPeriod}

import java.time.LocalDate

case class AccountingPeriod(
  accountingPeriodStartDate: Option[LocalDate],
  accountingPeriodEndDate: Option[LocalDate],
  turnover: Option[Int]
)

object AccountingPeriod {

  def create(accountingPeriod: IfAccountingPeriod): AccountingPeriod =
    AccountingPeriod(
      accountingPeriod.apStartDate.map(LocalDate.parse),
      accountingPeriod.apEndDate.map(LocalDate.parse),
      accountingPeriod.turnover
    )

  implicit val accountingPeriodWrites: Writes[AccountingPeriod] = (
    (JsPath \ "accountingPeriodStartDate").writeNullable[LocalDate] and
      (JsPath \ "accountingPeriodEndDate").writeNullable[LocalDate] and
      (JsPath \ "turnover").writeNullable[Int]
  )(unlift(AccountingPeriod.unapply))
}
