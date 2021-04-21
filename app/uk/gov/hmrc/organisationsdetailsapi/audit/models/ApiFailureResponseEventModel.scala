package uk.gov.hmrc.organisationsdetailsapi.audit.models

import play.api.libs.json.Json

case class ApiFailureResponseEventModel(deviceId: String,
                                        input: String,
                                        method: String,
                                        userAgent: String,
                                        apiVersion: String,
                                        matchId: String,
                                        correlationId: Option[String],
                                        requestUrl: String,
                                        response: String)

object ApiFailureResponseEventModel {
  implicit val formatApiFailureResponseEventModel = Json.format[ApiFailureResponseEventModel]
}

