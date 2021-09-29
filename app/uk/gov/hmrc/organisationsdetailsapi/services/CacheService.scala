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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import play.api.libs.json.Format
import uk.gov.hmrc.organisationsdetailsapi.cache.{CacheRepository, CacheRepositoryConfiguration}
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.NumberOfEmployeesRequest
import java.util.UUID

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class CacheService @Inject()(
                              cachingClient: CacheRepository,
                              conf: CacheRepositoryConfiguration)(implicit ec: ExecutionContext) {

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def get[T: Format](cacheId: CacheIdBase,
                     fallbackFunction: => Future[T]): Future[T] = {

    if (cacheEnabled)
      cachingClient.fetchAndGetEntry[T](cacheId.id) flatMap {
        case Some(value) =>
          Future.successful(value)
        case None =>
          fallbackFunction map { result =>
            cachingClient.cache(cacheId.id, result)
            result
          }
      } else {
      fallbackFunction
    }

  }
    def fetch[T: Format](matchId: UUID): Future[Option[T]] = {
      cachingClient.fetchAndGetEntry(matchId.toString) flatMap {
        result =>
          Future.successful(result)
      }
    }
}


trait CacheIdBase {
  val id: String
  override def toString: String = id

  def encodeVal(toEncode: String): String =
    BaseEncoding.base64().encode(toEncode.getBytes(Charsets.UTF_8))
}

case class CorporationTaxCacheId(matchId: UUID, cacheKey: String) extends CacheIdBase {
  lazy val id: String = s"$matchId-$cacheKey-corporation-tax"
}

case class NumberOfEmployeesCacheId(matchId: UUID, cacheKey: String, employeeCountRequest: NumberOfEmployeesRequest)
  extends CacheIdBase {

  lazy val from: String = employeeCountRequest.fromDate
  lazy val to: String = employeeCountRequest.toDate
  lazy val payeReferences: String = employeeCountRequest.payeReference.map(entry => entry.schemeReference + entry.districtNumber).reduce(_ + _)
  lazy val encoded: String = encodeVal(payeReferences)

  lazy val id: String = s"$matchId-$from-$to-$encoded-$cacheKey-number-of-employees"
}

case class SaCacheId(matchId: UUID, cacheKey: String) extends CacheIdBase {
  lazy val id: String = s"$matchId-$cacheKey-self-assessment"
}
