package uk.gov.hmrc.organisationsdetailsapi.audit.models

import play.api.libs.json.Json

case class IfApiResponseEventModel(deviceId: String,
                                   input: String,
                                   method: String,
                                   userAgent: String,
                                   apiVersion: String,
                                   matchId: String,
                                   correlationId: String,
                                   requestUrl: String,
                                   ifResponse: String)

object IfApiResponseEventModel {
  implicit val formatIfApiResponseEventModel = Json.format[IfApiResponseEventModel]
}

