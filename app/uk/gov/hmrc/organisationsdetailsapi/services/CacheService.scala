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

import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.cache.{CacheConfiguration, ShortLivedCache}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CacheService {

  val shortLivedCache: ShortLivedCache
  val conf: CacheConfiguration
  val key: String

  lazy val cacheEnabled: Boolean = conf.cacheEnabled

  def get[T: Format](cacheId: CacheIdBase, fallbackFunction: => Future[T])(implicit hc: HeaderCarrier): Future[T] =
    if (cacheEnabled) shortLivedCache.fetchAndGetEntry[T](cacheId.id, key) flatMap {
      case Some(value) =>
        Future.successful(value)
      case None =>
        fallbackFunction map { result =>
          shortLivedCache.cache(cacheId.id, key, result)
          result
        }
    } else {
      fallbackFunction
    }

}

@Singleton
class SaCacheService @Inject()(val shortLivedCache: ShortLivedCache, val conf: CacheConfiguration)
  extends CacheService {

  val key = conf.saKey

}

@Singleton
class CorporationTaxCacheService @Inject()(val shortLivedCache: ShortLivedCache, val conf: CacheConfiguration)
  extends CacheService {

  val key: String = conf.payeKey

}


trait CacheIdBase {
  val id: String
  override def toString: String = id
}

case class CorporationTaxCacheId(matchId: UUID, cacheKey: String) extends CacheIdBase {
  lazy val id: String = s"$matchId-$cacheKey-corporation-tax"
}

case class SaCacheId(matchId: UUID, cacheKey: String) extends CacheIdBase {
  lazy val id: String = s"$matchId-$cacheKey-self-assessment"
}
