/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationsMatchingConnector @Inject()(httpClient: HttpClient, servicesConfig: ServicesConfig) {

  private val serviceUrl = servicesConfig.baseUrl("organisations-matching-api")

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier, ec : ExecutionContext): Future[OrganisationMatch] =
    httpClient.GET[OrganisationMatch](s"$serviceUrl/match-record/$matchId")
}