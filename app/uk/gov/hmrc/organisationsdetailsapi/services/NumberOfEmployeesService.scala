/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.EmployeeCountRequest
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.{NumberOfEmployeesRequest, NumberOfEmployeesResponse}

import java.util.UUID
import javax.inject.{Inject, Named}
import scala.concurrent.{ExecutionContext, Future}

class NumberOfEmployeesService @Inject()(
                                          scopesHelper: ScopesHelper,
                                          scopesService: ScopesService,
                                          cacheService: CacheService,
                                          ifConnector: IfConnector,
                                          organisationsMatchingConnector: OrganisationsMatchingConnector,
                                          @Named("retryDelay") retryDelay: Int
                              )
  extends BaseService(retryDelay, organisationsMatchingConnector) {

  def get(matchId: UUID, employeeCountRequest: NumberOfEmployeesRequest, scopes: Iterable[String])
         (implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[Option[Seq[NumberOfEmployeesResponse]]] = {

    val endpointName = "number-of-employees"

    resolve(matchId).flatMap {
      organisationMatch =>
        val fieldsQuery = scopesHelper.getQueryStringFor(scopes.toList, endpointName)
        val cacheKey = scopesService.getValidFieldsForCacheKey(scopes.toList, Seq(endpointName))
        cacheService
          .get(
            cacheId = NumberOfEmployeesCacheId(matchId, cacheKey, employeeCountRequest),
            fallbackFunction = withRetry {
              ifConnector.getEmployeeCount(
                matchId.toString,
                organisationMatch.utr,
                EmployeeCountRequest.createFromRequest(employeeCountRequest),
                Some(fieldsQuery)
              )
            }
          ).map(x => x.references.map(
              y => y.map(NumberOfEmployeesResponse.create)
          )
        )
    }
  }
}
