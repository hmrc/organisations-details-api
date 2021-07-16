package uk.gov.hmrc.organisationsdetailsapi.audit.models

import play.api.libs.json.{JsValue, Json}

case class CorporationTaxApiResponseEventModel(deviceId: String,
                                               input: String,
                                               method: String,
                                               userAgent: String,
                                               apiVersion: String,
                                               matchId: String,
                                               correlationId: Option[String],
                                               applicationId: String,
                                               scopes: String,
                                               returnLinks: String,
                                               response: Option[JsValue])

object CorporationTaxApiResponseEventModel {
  implicit val formatCorporationTaxApiResponseEventModel = Json.format[CorporationTaxApiResponseEventModel]
}
