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

package unit.uk.gov.hmrc.organisationsdetailsapi.audit

import org.mockito.Mockito.{times, verify}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.audit.AuditHelper
import uk.gov.hmrc.organisationsdetailsapi.audit.models._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class AuditHelperSpec  extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val hc = HeaderCarrier()

  val auditConnector = mock[AuditConnector]
  val auditHelper = new AuditHelper(auditConnector)
  val correlationId = "test"
  val matchId = "80a6bb14-d888-436e-a541-4000674c60aa"
  val request = FakeRequest()
  val endpoint = "/test"
  val ifResponse = "bar"
  val nino = "CS700100A"
  val scopes = "test"
  val ifUrl =
    s"host/individuals/details/contact/nino/$nino?startDate=2019-01-01&endDate=2020-01-01&fields=some(vals(val1),val2)"

  "auditAuthScopes" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[ScopesAuditEventModel])

    auditHelper.auditAuthScopes(matchId, scopes, request)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("AuthScopesAuditEvent"),
      captor.capture())(any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ScopesAuditEventModel]
    capturedEvent.asInstanceOf[ScopesAuditEventModel].apiVersion shouldEqual "1.0"
    capturedEvent.asInstanceOf[ScopesAuditEventModel].matchId shouldEqual matchId
    capturedEvent.asInstanceOf[ScopesAuditEventModel].scopes shouldBe scopes

  }

  "auditApiFailure" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

    auditHelper.auditApiFailure(Some(correlationId), matchId, request, "/test", msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("ApiFailureEvent"),
      captor.capture())(any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ApiFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual endpoint
    capturedEvent.response shouldEqual msg

  }

  "auditIfApiResponse" in {

    Mockito.reset(auditConnector)

    val captor = ArgumentCaptor.forClass(classOf[IfApiResponseEventModel])

    auditHelper.auditIfApiResponse(correlationId, matchId, request, ifUrl, ifResponse)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiResponseEvent"),
      captor.capture())(any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[IfApiResponseEventModel]
    capturedEvent.asInstanceOf[IfApiResponseEventModel].matchId shouldEqual matchId
    capturedEvent.asInstanceOf[IfApiResponseEventModel].correlationId shouldEqual correlationId
    capturedEvent.asInstanceOf[IfApiResponseEventModel].requestUrl shouldBe ifUrl
    capturedEvent.asInstanceOf[IfApiResponseEventModel].ifResponse shouldBe ifResponse

  }

  "auditIfApiFailure" in {

    Mockito.reset(auditConnector)

    val msg = "Something went wrong"

    val captor = ArgumentCaptor.forClass(classOf[ApiFailureResponseEventModel])

    auditHelper.auditIfApiFailure(correlationId, matchId, request, ifUrl, msg)

    verify(auditConnector, times(1)).sendExplicitAudit(eqTo("IntegrationFrameworkApiFailureEvent"),
      captor.capture())(any(), any(), any())

    val capturedEvent = captor.getValue.asInstanceOf[ApiFailureResponseEventModel]
    capturedEvent.matchId shouldEqual matchId
    capturedEvent.correlationId shouldEqual Some(correlationId)
    capturedEvent.requestUrl shouldEqual ifUrl
    capturedEvent.response shouldEqual msg

  }
}
