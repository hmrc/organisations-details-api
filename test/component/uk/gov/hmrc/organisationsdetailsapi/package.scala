package component.uk.gov.hmrc

import play.api.libs.json.Json

package object organisationsdetailsapi {
  def errorResponse(code: String, message: String) =
    Json.obj("code" -> code, "message" -> message)
}
