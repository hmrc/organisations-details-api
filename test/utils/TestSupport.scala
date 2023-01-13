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

package utils

import akka.stream.Materializer
import akka.util.ByteString
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result

import java.nio.charset.Charset
import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.concurrent.{Await, Future}
import scala.language.{implicitConversions, postfixOps}

trait TestSupport {
  implicit val defaultTimeout: FiniteDuration = 5 seconds

  def await[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

  def bindModules: Seq[GuiceableModule] = Seq()

  // Convenience to avoid having to wrap andThen() parameters in Future.successful
  implicit def liftFuture[A](v: A): Future[A] = Future.successful(v)

  def status(of: Result): Int = of.header.status

  def status(of: Future[Result])(implicit timeout: Duration): Int = status(Await.result(of, timeout))

  def jsonBodyOf(result: Result)(implicit mat: Materializer): JsValue =
    Json.parse(bodyOf(result))

  def bodyOf(result: Result)(implicit mat: Materializer): String = {
    val bodyBytes: ByteString = await(result.body.consumeData)
    // We use the default charset to preserve the behaviour of a previous
    // version of this code, which used new String(Array[Byte]).
    // If the fact that the previous version used the default charset was an
    // accident then it may be better to decode in UTF-8 or the charset
    // specified by the result's headers.
    bodyBytes.decodeString(Charset.defaultCharset().name)
  }

  case class ExternalService(
                              serviceName: String,
                              runFrom: String = "SNAPSHOT_JAR",
                              classifier: Option[String] = None,
                              version: Option[String] = None)

}
