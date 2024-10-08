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

package it.uk.gov.hmrc.organisationsdetailsapi.cache.services

import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, OFormat}
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.services.{CacheIdBase, CacheService}

import scala.concurrent.Future

class CacheServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with OptionValues with IntegrationPatience {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  trait AppBuilder {
    val app: Application = new GuiceApplicationBuilder().configure("metrics.jvm" -> false).build()
  }

  "cache service" - {

    "sa" - {

      "must fetch an entry" in new AppBuilder {

        val cacheId: TestCacheId = TestCacheId("foo")

        running(app) {

          val svc = app.injector.instanceOf[CacheService]

          svc
            .get(cacheId, Future.successful(TestClass("bar")))
            .futureValue mustEqual TestClass("bar")

        }
      }

      "must fetch an existing entry when not expired " in new AppBuilder {

        val cacheId1: TestCacheId = TestCacheId("foo")
        val cacheId2: TestCacheId = TestCacheId("bar")

        running(app) {

          val svc = app.injector.instanceOf[CacheService]

          svc
            .get(cacheId1, Future.successful(TestClass("bar")))
            .futureValue mustEqual TestClass("bar")
          svc
            .get(cacheId1, Future.successful(TestClass("miss")))
            .futureValue mustEqual TestClass("bar")
          svc
            .get(cacheId2, Future.successful(TestClass("miss")))
            .futureValue mustEqual TestClass("miss")

        }
      }
    }

    "paye" - {

      "must fetch an entry" in new AppBuilder {

        val cacheId: TestCacheId = TestCacheId("one")

        running(app) {

          val svc = app.injector.instanceOf[CacheService]

          svc
            .get(cacheId, Future.successful(TestClass("bar")))
            .futureValue mustEqual TestClass("bar")

        }
      }

      "must fetch an existing entry when not expired " in new AppBuilder {

        val cacheId1: TestCacheId = TestCacheId("one")
        val cacheId2: TestCacheId = TestCacheId("two")

        running(app) {

          val svc = app.injector.instanceOf[CacheService]

          svc
            .get(cacheId1, Future.successful(TestClass("foo")))
            .futureValue mustEqual TestClass("bar")
          svc
            .get(cacheId1, Future.successful(TestClass("miss")))
            .futureValue mustEqual TestClass("bar")
          svc
            .get(cacheId2, Future.successful(TestClass("miss")))
            .futureValue mustEqual TestClass("miss")

        }
      }
    }
  }
}

case class TestCacheId(id: String) extends CacheIdBase

case class TestClass(param: String)

object TestClass {

  implicit val format: OFormat[TestClass] = Json.format[TestClass]

}
