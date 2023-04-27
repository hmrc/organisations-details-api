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

package unit.uk.gov.hmrc.organisationsdetailsapi.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import play.api.libs.json.Format
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.organisationsdetailsapi.cache.CacheRepositoryConfiguration
import uk.gov.hmrc.organisationsdetailsapi.connectors.{IfConnector, OrganisationsMatchingConnector}
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.CorporationTaxResponse
import uk.gov.hmrc.organisationsdetailsapi.domain.integrationframework.{CorporationTaxReturnDetailsResponse, AccountingPeriod => IFAccountingPeriod}
import uk.gov.hmrc.organisationsdetailsapi.domain.matching.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.services._

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class CorporationTaxServiceSpec extends AnyWordSpec with Matchers {

  private val stubbedCache = new CacheService(null, new CacheRepositoryConfiguration(Configuration())) {
    override def get[T: Format](cacheId: CacheIdBase, fallbackFunction: => Future[T]): Future[T] = {
      fallbackFunction
    }
  }

  trait Setup {
    val utr = "1234567890"
    val matchId = "9ff2e348-ee49-4e7e-8b73-17d02ff962a2"
    val matchIdUUID: UUID = UUID.fromString(matchId)

    val mockScopesHelper: ScopesHelper = mock[ScopesHelper]
    val mockScopesService: ScopesService = mock[ScopesService]
    val mockIfConnector: IfConnector = mock[IfConnector]
    val mockOrganisationsMatchingConnector: OrganisationsMatchingConnector = mock[OrganisationsMatchingConnector]

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val rh: RequestHeader = FakeRequest()

    val corporationTaxService: CorporationTaxService =
      new CorporationTaxService(
        mockScopesHelper,
        mockScopesService,
        stubbedCache,
        mockIfConnector,
        mockOrganisationsMatchingConnector,
        42
      )
  }

  "Corporation Tax Service" should {
    "get" should {

      "returns a valid payload when given a valid matchId" in new Setup {

        val endpoint = "corporation-tax"
        val scopes = Seq("SomeScope")

        when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
          .thenReturn(Future.successful(OrganisationMatch(matchIdUUID, utr)))

        when(mockScopesHelper.getQueryStringFor(scopes, endpoint))
          .thenReturn("ABC")

        when(mockScopesService.getValidFieldsForCacheKey(scopes.toList, Seq(endpoint)))
          .thenReturn("DEF")

        when(mockIfConnector.getCtReturnDetails(matchId, utr, Some("ABC")))
          .thenReturn(Future.successful(CorporationTaxReturnDetailsResponse(
            Some(utr),
            Some("2015-04-21"),
            Some("V"),
            Some(Seq(
              IFAccountingPeriod(Some("2018-04-06"), Some("2018-10-05"), Some(2340)),
              IFAccountingPeriod(Some("2018-10-06"), Some("2019-04-05"), Some(2340))
            ))
          )))

        val response: CorporationTaxResponse = Await.result(corporationTaxService.get(matchIdUUID, endpoint, scopes), 5 seconds)

        response.dateOfRegistration.get shouldBe LocalDate.of(2015, 4, 21)
        response.taxSolvencyStatus.get shouldBe "V"
        response.accountingPeriods.get.length shouldBe 2

      }

      "Return a failed future if IF or cache throws exception" in new Setup {
        val endpoint = "corporation-tax"
        val scopes = Seq("SomeScope")

        when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
          .thenReturn(Future.successful(OrganisationMatch(matchIdUUID, utr)))

        when(mockScopesHelper.getQueryStringFor(scopes, endpoint))
          .thenReturn("ABC")

        when(mockScopesService.getValidFieldsForCacheKey(scopes.toList, Seq(endpoint)))
          .thenReturn("DEF")

        when(mockIfConnector.getCtReturnDetails(matchId, utr, Some("ABC")))
          .thenReturn(Future.failed(new Exception()))

        assertThrows[Exception] {
          Await.result(corporationTaxService.get(matchIdUUID, endpoint, scopes), 10 seconds)
        }
      }

      "propagates not found when match id can not be found" in new Setup {
        val endpoint = "corporation-tax"
        val scopes = Seq("SomeScope")

        when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
          .thenReturn(Future.failed(new NotFoundException("NOT_FOUND")))

        assertThrows[NotFoundException] {
          Await.result(corporationTaxService.get(matchIdUUID, endpoint, scopes), 10 seconds)
        }
      }

      "retries once if IF returns error" in new Setup {
        val endpoint = "corporation-tax"
        val scopes = Seq("SomeScope")

        when(mockOrganisationsMatchingConnector.resolve(matchIdUUID))
          .thenReturn(Future.successful(OrganisationMatch(matchIdUUID, utr)))

        when(mockScopesHelper.getQueryStringFor(scopes, endpoint))
          .thenReturn("ABC")

        when(mockScopesService.getValidFieldsForCacheKey(scopes.toList, Seq(endpoint)))
          .thenReturn("DEF")

        when(mockIfConnector.getCtReturnDetails(matchId, utr, Some("ABC")))
          .thenReturn(Future.failed(UpstreamErrorResponse("""¯\_(ツ)_/¯""", 503, 503)))
          .thenReturn(Future.successful(CorporationTaxReturnDetailsResponse(
            Some(utr),
            Some("2015-04-21"),
            Some("V"),
            Some(Seq(
              IFAccountingPeriod(Some("2018-04-06"), Some("2018-10-05"), Some(2340)),
              IFAccountingPeriod(Some("2018-10-06"), Some("2019-04-05"), Some(2340))
            ))
          )))

        val response: CorporationTaxResponse = Await.result(corporationTaxService.get(matchIdUUID, endpoint, scopes), 5 seconds)

        verify(mockIfConnector, times(2))
          .getCtReturnDetails(any(), any(), any())(any(), any(), any())

        response.dateOfRegistration.get shouldBe LocalDate.of(2015, 4, 21)
        response.taxSolvencyStatus.get shouldBe "V"
        response.accountingPeriods.get.length shouldBe 2

      }
    }
  }
}
