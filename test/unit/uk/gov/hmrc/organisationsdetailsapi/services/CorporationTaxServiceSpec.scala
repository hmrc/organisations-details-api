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

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Format
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.organisationsdetailsapi.cache.{CacheConfiguration, ShortLivedCache}
import uk.gov.hmrc.organisationsdetailsapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{AccountingPeriod, CorporationTaxReturnDetailsResponse}
import uk.gov.hmrc.organisationsdetailsapi.services._

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CorporationTaxServiceSpec extends AsyncWordSpec with Matchers {

  val stubbedCache = new CacheService {
    override def get[T: Format](cacheId: CacheIdBase, fallbackFunction: => Future[T])(implicit hc: HeaderCarrier): Future[T] = {
      fallbackFunction
    }

    override val shortLivedCache: ShortLivedCache = null
    override val conf: CacheConfiguration = null
    override val key: String = null
  }

  "Sandbox Corporation Tax Service" should {

  }

  "Live Corporation Tax Service" should {

    val utr = "1234567890"
    val matchId = "9ff2e348-ee49-4e7e-8b73-17d02ff962a2"
    val matchIdUUID = UUID.fromString(matchId)

    val mockScopesHelper = mock[ScopesHelper]
    val mockScopesService = mock[ScopesService]
    val mockIfConnector = mock[IfConnector]
    val mockOrganisationsMatchingConnector = mock[OrganisationsMatchingConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val rh: RequestHeader = FakeRequest()

    val liveCorporationTaxService: LiveCorporationTaxService =
      new LiveCorporationTaxService(
        mockScopesHelper,
        mockScopesService,
        stubbedCache,
        mockIfConnector,
        mockOrganisationsMatchingConnector,
        42
      )

    "returns a valid payload when given a valid matchId" in {

      val endpoint = "corporation-tax"
      val scopes = Seq("SomeScope")

      when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
        .thenReturn(Future.successful(OrganisationMatch(matchIdUUID, utr)))

      when(mockScopesHelper.getQueryStringFor(scopes, endpoint))
        .thenReturn("ABC")

      when(mockScopesService.getValidFieldsForCacheKey(scopes.toList))
        .thenReturn("DEF")

      when(mockIfConnector.getCtReturnDetails(matchId, utr, Some("ABC")))
        .thenReturn(Future.successful(CorporationTaxReturnDetailsResponse(
          Some(utr),
          Some("2015-04-21"),
          Some("V"),
          Some(Seq(
            AccountingPeriod(Some("2018-04-06"), Some("2018-10-05"), Some(2340)),
            AccountingPeriod(Some("2018-10-06"), Some("2019-04-05"), Some(2340))
          ))
        )))

      val res = liveCorporationTaxService.get(matchIdUUID,endpoint, scopes)

      res.map(response => {
        response.dateOfRegistration.get shouldBe LocalDate.of(2015, 4, 21)
        response.taxSolvencyStatus.get shouldBe "V"
        response.periods.get.length shouldBe 2
      })

    }

    "Return a failed future if IF or cache throws exception" in {
      val endpoint = "corporation-tax"
      val scopes = Seq("SomeScope")

      when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
        .thenReturn(Future.successful(OrganisationMatch(matchIdUUID, utr)))

      when(mockScopesHelper.getQueryStringFor(scopes, endpoint))
        .thenReturn("ABC")

      when(mockScopesService.getValidFieldsForCacheKey(scopes.toList))
        .thenReturn("DEF")

      when(mockIfConnector.getCtReturnDetails(matchId, utr, Some("ABC")))
        .thenReturn(Future.failed(new Exception()))

      assertThrows[Exception] {
        Await.result(liveCorporationTaxService.get(matchIdUUID, endpoint, scopes), 10 seconds)
      }
    }

    "propagates not found when match id can not be found" in {
      val endpoint = "corporation-tax"
      val scopes = Seq("SomeScope")

      when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
        .thenThrow(new NotFoundException("NOT FOUND"))

      assertThrows[NotFoundException] {
        Await.result(liveCorporationTaxService.get(matchIdUUID, endpoint, scopes), 10 seconds)
      }
    }

  }
}
