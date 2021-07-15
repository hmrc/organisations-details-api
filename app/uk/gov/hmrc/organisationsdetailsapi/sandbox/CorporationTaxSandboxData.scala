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

package uk.gov.hmrc.organisationsdetailsapi.sandbox

import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.{AccountingPeriod, CorporationTaxResponse}

import java.time.LocalDate
import java.util.UUID

object CorporationTaxSandboxData {
  val sandboxMatchId: String = "ee7e0f90-18eb-4a25-a3ac-77f27beb2f0f"
  val sandboxMatchIdUUID: UUID = UUID.fromString(sandboxMatchId)

  val sandboxReturnData: CorporationTaxResponse = CorporationTaxResponse(
    dateOfRegistration = Some(LocalDate.of(2015, 4, 21)),
    taxSolvencyStatus = Some("V"),
    periods = Some(Seq(
      AccountingPeriod(
        accountingPeriodStartDate = Some(LocalDate.of(2018, 4, 6)),
        accountingPeriodEndDate = Some(LocalDate.of(2018, 10, 5)),
        turnover = Some(38390)
      ),
      AccountingPeriod(
        accountingPeriodStartDate = Some(LocalDate.of(2018, 10, 6)),
        accountingPeriodEndDate = Some(LocalDate.of(2019, 4, 5)),
        turnover = Some(2340)
      )
    ))
  )
}
