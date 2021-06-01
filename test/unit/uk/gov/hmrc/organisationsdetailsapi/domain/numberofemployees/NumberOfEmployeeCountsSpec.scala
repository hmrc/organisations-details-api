package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeeCounts

class NumberOfEmployeeCountsSpec extends AnyWordSpec with Matchers {
  "Writes to json successfully" in {
    val expectedJson =
      """
        |{
        |   "dateOfCount": "2019-03",
        |   "numberOfEmployees": "1234"
        |}
        |""".stripMargin

    val selfAssessmentResponse = NumberOfEmployeeCounts(1234, "2019-03")

    val expectedResponse = Json.parse(expectedJson)

    val result = Json.toJson(selfAssessmentResponse)

    result shouldBe expectedResponse
  }
}
