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
import uk.gov.hmrc.organisationsdetailsapi.connectors.IfConnector
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.CorporationTaxResponse
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.CorporationTaxReturnDetails._

import java.util.UUID
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

trait CorporationTaxService {

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch]

  def get(matchId: UUID, endpoint: String, scopes: Iterable[String])
          (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[CorporationTaxResponse]
}

class SandboxCorporationTaxService extends CorporationTaxService {
  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch] = ???

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[CorporationTaxResponse] = ???
}

class LiveCorporationTaxService @Inject()(
                                         scopesHelper: ScopesHelper,
                                         scopesService: ScopesService,
                                         cacheService: CacheService,
                                         ifConnector: IfConnector,
                                         @Named("retryDelay") retryDelay: Int
                                         )extends CorporationTaxService {

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch] = {
    // TODO IMPLEMENT PROPERLY
    Future.successful(OrganisationMatch(UUID.randomUUID(), "NOT A UTR"))
  }

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[CorporationTaxResponse] = {
    resolve(matchId).flatMap {
      organisationMatch =>
        val fieldsQuery = scopesHelper.getQueryStringFor(scopes.toList, endpoint)
        val cacheKey = scopesService.getValidFieldsForCacheKey(scopes.toList)
        cacheService
          .get(
            cacheId = CorporationTaxCacheId(matchId, cacheKey),
            fallbackFunction = withRetry {
              ifConnector.getCtReturnDetails(
                matchId.toString,
                organisationMatch.utr,
                Some(fieldsQuery)
              )
            }
          ).map(CorporationTaxResponse.create)
    }
  }

  private def withRetry[T](body: => Future[T])(implicit ec: ExecutionContext): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
