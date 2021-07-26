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

package uk.gov.hmrc.organisationsdetailsapi.services

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.organisationsdetailsapi.connectors.OrganisationsMatchingConnector
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseService(retryDelay: Int, organisationsMatchingConnector: OrganisationsMatchingConnector) {

  protected def resolve(matchId: UUID)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganisationMatch] =
    organisationsMatchingConnector.resolve(matchId)

  protected def withRetry[T](body: => Future[T])(implicit ec: ExecutionContext): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
