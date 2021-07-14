package uk.gov.hmrc.organisationsdetailsapi.services

import java.util.UUID

import javax.inject.Inject
import org.joda.time.Interval
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.organisationsdetailsapi.connectors.IfConnector
import uk.gov.hmrc.organisationsdetailsapi.domain.OrganisationMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.CorporationTaxResponse

import scala.concurrent.{ExecutionContext, Future}

trait CorporationTaxService {

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch]

  def get(matchId: UUID, endpoint: String, scopes: Iterable[String])
          (implicit hc: HeaderCarrier, request: RequestHeader): Future[CorporationTaxResponse]
}

class SandboxCorporationTaxService extends CorporationTaxService {
  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch] = {

  }

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader): Future[CorporationTaxResponse] = {

  }
}

class LiveCorporationTaxService @Inject()(
                                         scopesHelper: ScopesHelper,
                                         scopesService: ScopesService,
                                         cacheService: CacheService,
                                         ifConnector: IfConnector
                                         )extends CorporationTaxService {

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[OrganisationMatch] = {

  }

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader, ec: ExecutionContext): Future[CorporationTaxResponse] = {
    resolve(matchId).flatMap {
      organisationMatch =>
        val fieldsQuery = scopesHelper.getQueryStringFor(scopes.toList, endpoint)
        val cacheKey = scopesService.getValidFieldsForCacheKey(scopes.toList)
        cacheService
          .get(
            cacheId = CorporationTaxCacheId(matchId, cacheKey),
            fallbackFunction = withRetry {
              ifConnector.getCtReturnDetails(
                matchId.toString,
                organisationMatch.utr,
                Some(fieldsQuery)
              )
            }
          )
    }
  }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
