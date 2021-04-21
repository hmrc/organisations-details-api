package uk.gov.hmrc.organisationsdetailsapi.audit.models

import play.api.libs.json.Json

case class ApiResponseEventModel(deviceId: String,
                                 input: String,
                                 method: String,
                                 userAgent: String,
                                 apiVersion: String,
                                 matchId: String,
                                 correlationId: Option[String],
                                 scopes: String,
                                 returnLinks: String)

object ApiResponseEventModel {
  implicit val formatApiResponseEventModel = Json.format[ApiResponseEventModel]
}