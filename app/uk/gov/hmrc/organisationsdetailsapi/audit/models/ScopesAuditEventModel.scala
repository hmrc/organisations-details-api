package uk.gov.hmrc.organisationsdetailsapi.audit.models

import play.api.libs.json.Json

case class ScopesAuditEventModel(deviceId: String,
                                 input: String,
                                 method: String,
                                 userAgent: String,
                                 apiVersion: String,
                                 matchId: String,
                                 scopes: String)

object ScopesAuditEventModel {
  implicit val formatScopesAuditEventModel = Json.format[ScopesAuditEventModel]
}


