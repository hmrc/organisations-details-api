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

package uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.PayeReferenceAndCount

case class NumberOfEmployeesResponse(payeReference: Option[String], counts: Option[Seq[NumberOfEmployeeCounts]])

object NumberOfEmployeesResponse {
  def create(payeReferenceAndCount: PayeReferenceAndCount) : NumberOfEmployeesResponse =
    NumberOfEmployeesResponse(
      payeReferenceAndCount.payeReference,
      payeReferenceAndCount.counts.map(x => x.map(NumberOfEmployeeCounts.create))
    )

  implicit val numberOfEmployeesResponseWrites : Writes[NumberOfEmployeesResponse] =
    (
      (JsPath \ "payeReference").writeNullable[String] and
        (JsPath \ "counts").writeNullable[Seq[NumberOfEmployeeCounts]]
      )(unlift(NumberOfEmployeesResponse.unapply))
}