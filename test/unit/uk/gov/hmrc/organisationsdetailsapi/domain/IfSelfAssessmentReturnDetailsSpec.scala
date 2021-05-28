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

package unit.uk.gov.hmrc.organisationsdetailsapi.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.IfSelfAssessmentReturnDetails

class IfSelfAssessmentReturnDetailsSpec extends AnyWordSpec with Matchers {
  "IfSelfAssessmentReturnDetailsSpec read from JSON successfully" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val expectedResult = IfSelfAssessmentReturnDetails("1234567890", "2015-04-21", "Individual", "S", Seq.empty)

    val result = Json.parse(json).validate[IfSelfAssessmentReturnDetails]

    result.isSuccess shouldBe true
    result.get shouldBe expectedResult
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if tax solvency status is not S or I" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "M",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[IfSelfAssessmentReturnDetails]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid start date" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "20111-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[IfSelfAssessmentReturnDetails]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid utr" in {
    val json =
      """
        |{
        |  "utr": "12345678901",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[IfSelfAssessmentReturnDetails]

    result.isSuccess shouldBe false
  }

  "CreateSelfAssessmentReturnDetailRequest read from JSON unsuccessfully if given an invalid taxPayerType" in {
    val json =
      """
        |{
        |  "utr": "1234567890",
        |  "startDate": "2015-04-21",
        |  "taxpayerType": "2Individual",
        |  "taxSolvencyStatus": "S",
        |  "taxyears": []
        |}""".stripMargin

    val result = Json.parse(json).validate[IfSelfAssessmentReturnDetails]

    result.isSuccess shouldBe false
  }

}
