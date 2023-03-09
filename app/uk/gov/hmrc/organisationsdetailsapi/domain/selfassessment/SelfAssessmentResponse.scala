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

package uk.gov.hmrc.organisationsdetailsapi.domain.selfassessment

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.SelfAssessmentReturnDetailResponse

import java.time.LocalDate

case class SelfAssessmentResponse(selfAssessmentStartDate: Option[LocalDate], taxSolvencyStatus: Option[String], taxReturns: Option[Seq[SelfAssessmentReturn]])

object SelfAssessmentResponse {

  def create(selfAssessmentReturnDetailsResponse: SelfAssessmentReturnDetailResponse): SelfAssessmentResponse =
    SelfAssessmentResponse(
      selfAssessmentReturnDetailsResponse.startDate.map(LocalDate.parse),
      selfAssessmentReturnDetailsResponse.taxSolvencyStatus,
      selfAssessmentReturnDetailsResponse.taxYears.map(x => x.map(t => SelfAssessmentReturn(t.businessSalesTurnover, t.taxyear)))
    )

  implicit val selfAssessmentResponseWrites: Writes[SelfAssessmentResponse] = (
    (JsPath \ "selfAssessmentStartDate").writeNullable[LocalDate] and
      (JsPath \ "taxSolvencyStatus").writeNullable[String] and
      (JsPath \ "taxReturns").writeNullable[Seq[SelfAssessmentReturn]]
    )(unlift(SelfAssessmentResponse.unapply))
}
