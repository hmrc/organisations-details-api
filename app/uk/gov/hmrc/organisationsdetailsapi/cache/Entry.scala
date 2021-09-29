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

package uk.gov.hmrc.organisationsdetailsapi.cache

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath}

case class Entry(id: String, data: Data, modifiedDetails: ModifiedDetails)

object Entry {
  implicit val format: Format[Entry] = Format(
    (
      (JsPath \ "id").read[String] and
        (JsPath \ "data").read[Data] and
        (JsPath \ "modifiedDetails").read[ModifiedDetails]
      )(Entry.apply _),
    (
      (JsPath \ "id").write[String] and
        (JsPath \ "data").write[Data] and
        (JsPath \ "modifiedDetails").write[ModifiedDetails]
      )(unlift(Entry.unapply))
  )
}
