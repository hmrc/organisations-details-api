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
      utr = "1234567890",
      taxpayerStartDate =  "2015-04-21",
      taxSolvencyStatus =  "V",
      accountingPeriods =  Seq(
      AccountingPeriod(
        apStartDate = "2018-04-06",
        apEndDate = "2018-10-05",
        turnover = 38390
      ),
      AccountingPeriod(
        apStartDate = "2018-10-06",
        apEndDate = "2019-04-05",
        turnover = 2340
      ))
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
      startDate =  "2019-10-01",
      endDate =  "2020-04-05",
      references =  Seq(
        PayeReferenceAndCount(
          districtNumber =  "456",
          payeReference =  "RT882d",
          counts = Seq(
          Count(
            dateTaken = "2019-10",
            employeeCount = 1234
          ),
          Count(
            dateTaken = "2019-11",
            employeeCount = 1466
          ),
          Count(
            dateTaken = "2019-12",
            employeeCount = 1765
          ),
          Count(
            dateTaken = "2020-01",
            employeeCount = 1666
          ),
          Count(
            dateTaken = "2020-02",
            employeeCount = 1589
          ),
          Count(
            dateTaken = "2020-03",
            employeeCount = 1555
          ),
        )),
        PayeReferenceAndCount(
          districtNumber =  "123",
          payeReference =  "AB888666",
          counts = Seq(
          Count(
            dateTaken = "2019-10",
            employeeCount = 554
          ),
          Count(
            dateTaken = "2019-11",
            employeeCount = 567
          ),
          Count(
            dateTaken = "2019-12",
            employeeCount = 599
          ),
          Count(
            dateTaken = "2020-01",
            employeeCount = 571
          ),
          Count(
            dateTaken = "2020-02",
            employeeCount = 566
          ),
          Count(
            dateTaken = "2020-03",
            employeeCount = 555
          )
        )
      )
      )
    )
  }

  def createValidSelfAssessmentReturnDetails(): SelfAssessmentReturnDetailResponse = {
    SelfAssessmentReturnDetailResponse(
      utr = "1234567890",
      taxPayerType = "Individual",
      startDate = "2015-04-21",
      taxSolvencyStatus = "S",
      taxYears = Seq(
      TaxYear(
        taxYear = "2020",
        businessSalesTurnover = 38390.76
      ),
      TaxYear(
        taxYear = "2019",
        businessSalesTurnover = 12343.12
      ),
      TaxYear(
        taxYear = "2018",
        businessSalesTurnover = 20182.22
      ),
      TaxYear(
        taxYear = "2017",
        businessSalesTurnover = 20177.77
      ),
      TaxYear(
        taxYear = "2016",
        businessSalesTurnover = 20166.66
      ),
      TaxYear(
        taxYear = "2015",
        businessSalesTurnover = 20155.55
      ),
      TaxYear(
        taxYear = "2014",
        businessSalesTurnover = 20144.5
      )
      )
    )
  }
}
