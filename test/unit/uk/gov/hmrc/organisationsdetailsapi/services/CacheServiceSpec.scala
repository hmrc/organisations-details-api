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

package unit.uk.gov.hmrc.organisationsdetailsapi.services

import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{verify, verifyNoInteractions}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.cache.{CacheConfiguration, ShortLivedCache}
import uk.gov.hmrc.organisationsdetailsapi.services.{CacheIdBase, CacheService, CorporationTaxCacheId, SaCacheId}
import utils.TestSupport

import java.util.UUID
import scala.concurrent.Future

class CacheServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with TestSupport {

  val cacheId = TestCacheId("foo")
  val cachedValue = TestClass("cached value")
  val newValue = TestClass("new value")

  trait Setup {

    val mockClient = mock[ShortLivedCache]
    val mockCacheConfig = mock[CacheConfiguration]
    val cacheService = new CacheService {
      override val shortLivedCache: ShortLivedCache = mockClient
      override val conf: CacheConfiguration = mockCacheConfig
      override val key: String = "test"
    }

    implicit val hc: HeaderCarrier = HeaderCarrier()

    given(mockCacheConfig.cacheEnabled).willReturn(true)

  }

  "cacheService.get" should {

    "return the cached value for a given id and key" in new Setup {

      given(mockClient.fetchAndGetEntry[TestClass](eqTo(cacheId.id), eqTo(cacheService.key))(any()))
        .willReturn(Future.successful(Some(cachedValue)))
      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe cachedValue

    }

    "cache the result of the fallback function when no cached value exists for a given id and key" in new Setup {

      given(mockClient.fetchAndGetEntry[TestClass](eqTo(cacheId.id), eqTo(cacheService.key))(any()))
        .willReturn(Future.successful(None))

      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe newValue
      verify(mockClient).cache[TestClass](eqTo(cacheId.id), eqTo(cacheService.key), eqTo(newValue))(any())

    }

    "ignore the cache when caching is not enabled" in new Setup {

      given(mockCacheConfig.cacheEnabled).willReturn(false)
      await(cacheService.get[TestClass](cacheId, Future.successful(newValue))) shouldBe newValue
      verifyNoInteractions(mockClient)

    }
  }

  "CorporationTaxCacheId" should {

    "produce a cache id based on matchId" in {

      val matchId = UUID.randomUUID()

      CorporationTaxCacheId(matchId, "ABC").id shouldBe
        s"$matchId-ABC-corporation-tax"

    }

  }

  "SaCacheId" should {

    "produce a cache id based on matchId" in {

      val matchId = UUID.randomUUID()

      SaCacheId(matchId).id shouldBe
        s"$matchId-self-assessment"

    }

  }
}

case class TestCacheId(id: String) extends CacheIdBase

case class TestClass(value: String)

object TestClass {

  implicit val format: OFormat[TestClass] = Json.format[TestClass]

}
