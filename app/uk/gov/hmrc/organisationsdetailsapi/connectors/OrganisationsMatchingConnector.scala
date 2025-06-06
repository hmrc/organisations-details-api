/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.organisationsdetailsapi.connectors

import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.OrganisationMatch._
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.{OrganisationMatch, OrganisationVatMatch}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationsMatchingConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig) {

  private val serviceUrl = servicesConfig.baseUrl("organisations-matching-api")

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganisationMatch] = {
    httpClient
      .get(url"$serviceUrl/match-record/$matchId")
      .execute[OrganisationMatch]
  }

  def resolveVat(matchId: UUID)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganisationVatMatch] = {
    httpClient
      .get(url"$serviceUrl/match-record/vat/$matchId")
      .execute[OrganisationVatMatch]
  }
}
