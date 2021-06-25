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

package utils

import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework._

trait IfHelpers {

  def createValidCorporationTaxReturnDetails(): CorporationTaxReturnDetailsResponse = {
    CorporationTaxReturnDetailsResponse(
      utr = Some("1234567890"),
      taxpayerStartDate =  Some("2015-04-21"),
      taxSolvencyStatus =  Some("V"),
      accountingPeriods =  Some(Seq(
      AccountingPeriod(
        apStartDate = Some("2018-04-06"),
        apEndDate = Some("2018-10-05"),
        turnover = Some(38390)
      ),
      AccountingPeriod(
        apStartDate = Some("2018-10-06"),
        apEndDate = Some("2019-04-05"),
        turnover =Some(2340)
      )))
    )
  }

  def createValidEmployeeCountRequest(): EmployeeCountRequest = {
    EmployeeCountRequest(
        startDate =  "2019-10-01",
        endDate =  "2020-04-05",
        references =  Seq(
        PayeReference(
          districtNumber =  "456",
          payeReference =  "RT882d"
        ),
        PayeReference(
          districtNumber =  "123",
          payeReference =  "AB888666"
        )
      )
    )
  }

  def createValidEmployeeCountResponse(): EmployeeCountResponse = {
    EmployeeCountResponse(
      startDate =  Some("2019-10-01"),
      endDate =  Some("2020-04-05"),
      references =  Some(Seq(
        PayeReferenceAndCount(
          districtNumber =  Some("456"),
          payeReference =  Some("RT882d"),
          counts = Some(Seq(
          Count(
            dateTaken = Some("2019-10"),
            employeeCount = Some(1234)
          ),
          Count(
            dateTaken = Some("2019-11"),
            employeeCount = Some(1466)
          ),
          Count(
            dateTaken = Some("2019-12"),
            employeeCount = Some(1765)
          ),
          Count(
            dateTaken = Some("2020-01"),
            employeeCount = Some(1666)
          ),
          Count(
            dateTaken = Some("2020-02"),
            employeeCount = Some(1589)
          ),
          Count(
            dateTaken = Some("2020-03"),
            employeeCount = Some(1555)
          ),
        ))),
        PayeReferenceAndCount(
          districtNumber =  Some("123"),
          payeReference =  Some("AB888666"),
          counts = Some(Seq(
          Count(
            dateTaken = Some("2019-10"),
            employeeCount = Some(554)
          ),
          Count(
            dateTaken = Some("2019-11"),
            employeeCount = Some(567)
          ),
          Count(
            dateTaken = Some("2019-12"),
            employeeCount = Some(599)
          ),
          Count(
            dateTaken = Some("2020-01"),
            employeeCount = Some(571)
          ),
          Count(
            dateTaken = Some("2020-02"),
            employeeCount = Some(566)
          ),
          Count(
            dateTaken = Some("2020-03"),
            employeeCount = Some(555)
          )
        )
      )
    ))))
  }

  def createValidSelfAssessmentReturnDetails(): SelfAssessmentReturnDetailResponse = {
    SelfAssessmentReturnDetailResponse(
      utr = Some("1234567890"),
      taxPayerType = Some("Individual"),
      startDate = Some("2015-04-21"),
      taxSolvencyStatus = Some("S"),
      taxYears = Some(Seq(
        TaxYear(
          taxYear = Some("2020"),
          businessSalesTurnover = Some(38390.76)
        ),
        TaxYear(
          taxYear = Some("2019"),
          businessSalesTurnover = Some(12343.12)
        ),
        TaxYear(
          taxYear = Some("2018"),
          businessSalesTurnover = Some(20182.22)
        ),
        TaxYear(
          taxYear = Some("2017"),
          businessSalesTurnover = Some(20177.77)
        ),
        TaxYear(
          taxYear = Some("2016"),
          businessSalesTurnover = Some(20166.66)
        ),
        TaxYear(
          taxYear = Some("2015"),
          businessSalesTurnover = Some(20155.55)
        ),
        TaxYear(
          taxYear = Some("2014"),
          businessSalesTurnover = Some(20144.5)
        )
      ))
    )
  }
}
