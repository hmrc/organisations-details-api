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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.organisationsdetailsapi.domain.numberofemployees.{NumberOfEmployeesRequest, PayeReference}
import uk.gov.hmrc.organisationsdetailsapi.services.NumberOfEmployeesCacheId

import java.util.UUID

class CacheIdSpec extends AnyWordSpec with Matchers  {

  private val sampleValidRequest = NumberOfEmployeesRequest(
    "2019-10-01",
    "2020-04-05",
    Seq(
      PayeReference("456", "RT882d"),
      PayeReference("457", "RT882e"),
      PayeReference("458", "RT882f")
    )
  )

  private val uuid = UUID.fromString("fcb6218d-0f90-4c5d-bb58-6b128d30ac04")

  "Cache Id" should {
    "Generate string correctly" in {
      NumberOfEmployeesCacheId(uuid, "ABC", sampleValidRequest).toString shouldBe
        "fcb6218d-0f90-4c5d-bb58-6b128d30ac04-2019-10-01-2020-04-05-UlQ4ODJkNDU2UlQ4ODJlNDU3UlQ4ODJmNDU4-ABC-number-of-employees"
    }
  }
}
