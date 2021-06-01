package unit.uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeesResponse

class NumberOfEmployeesResponseSpec extends AnyWordSpec with Matchers {
  "Writes to json successfully" in {
    val expectedJson =
      """
        |{
        |  "payeReference": "RT882d/456",
        |  "counts": []
        |""".stripMargin

    val selfAssessmentResponse = NumberOfEmployeesResponse("RT882d/456", Seq.empty)

    val expectedResponse = Json.parse(expectedJson)

    val result = Json.toJson(selfAssessmentResponse)

    result shouldBe expectedResponse
  }
}
