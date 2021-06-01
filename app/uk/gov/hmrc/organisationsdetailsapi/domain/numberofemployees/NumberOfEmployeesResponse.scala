package uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class NumberOfEmployeesResponse(payeReference: String, counts: Seq[NumberOfEmployeeCounts])

object NumberOfEmployeesResponse {
  implicit val numberOfEmployeesResponseWrites : Writes[NumberOfEmployeesResponse] =
    (
      (JsPath \ "payeReference").write[String] and
        (JsPath \ "counts").write[Seq[NumberOfEmployeeCounts]]
      )(unlift(NumberOfEmployeesResponse.unapply))
}