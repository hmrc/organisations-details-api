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

package unit.uk.gov.hmrc.organisationsdetailsapi.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{FakeRequest, Helpers}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.config.AppConfig
import uk.gov.hmrc.organisationsdetailsapi.controllers.CorporationTaxController
import uk.gov.hmrc.organisationsdetailsapi.services.CorporationTaxService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class CorporationTaxControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar {

  private val fakeRequest = FakeRequest("GET", "/")

  private val env           = Environment.simple()
  private val configuration = Configuration.load(env)

  private val mockAuthConnector = mock[AuthConnector]
  private val mockDetailsService = mock[CorporationTaxService]

  private val serviceConfig = new ServicesConfig(configuration)
  private val appConfig     = new AppConfig(configuration, serviceConfig)

  private val controller = new CorporationTaxController()(mockAuthConnector, Helpers.stubControllerComponents(), mockDetailsService)


}