package uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class NumberOfEmployeeCounts(numberOfEmployees: Int, dateOfCount: String)

object NumberOfEmployeeCounts {
  implicit val numberOfEmployeeCountsWrites : Writes[NumberOfEmployeeCounts] =
    (
      (JsPath \ "numberOfEmployees").write[Int] and
        (JsPath \ "dateOfCount").write[String]
      )(unlift(NumberOfEmployeeCounts.unapply))
}