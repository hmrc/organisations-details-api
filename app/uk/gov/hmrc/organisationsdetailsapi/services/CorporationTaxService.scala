package uk.gov.hmrc.organisationsdetailsapi.services

import java.util.UUID
import javax.inject.Inject
import org.joda.time.Interval
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.organisationsdetailsapi.domain.NinoMatch
import uk.gov.hmrc.organisationsdetailsapi.domain.corporationtax.CorporationTaxResponse

import scala.concurrent.Future

trait CorporationTaxService {

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def get(matchId: UUID, endpoint: String, scopes: Iterable[String])
          (implicit hc: HeaderCarrier, request: RequestHeader): Future[CorporationTaxResponse]
}

class SandboxCorporationTaxService extends CorporationTaxService {
  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] = {

  }

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader): Future[CorporationTaxResponse] = {

  }
}

class LiveCorporationTaxService @Inject()(
                                         scopesHelper: ScopesHelper,
                                         scopesService: ScopesService,
                                         cacheService: CacheService
                                         )extends CorporationTaxService {

  override def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch] = {

  }

  override def get(matchId: UUID, endpoint: String, scopes: Iterable[String])(implicit hc: HeaderCarrier, request: RequestHeader): Future[CorporationTaxResponse] = {
    resolve(matchId).flatMap {
      ninoMatch =>
        val fieldsQuery = scopesHelper.getQueryStringFor(scopes.toList, endpoint)
        val cacheKey = scopesService.getValidFieldsForCacheKey(scopes.toList)
        cacheService
          .get(
            cacheId = CorporationTaxCacheId(matchId, cacheKey),
            fallbackFunction = withRetry {
              ifConnector.fetchEmployments(
                ninoMatch.nino,
                Option(fieldsQuery).filter(_.nonEmpty),
                matchId.toString
              )
            }
          )
          .map {
            _.map(Employment.create).filter(_.isDefined).map(_.get)
          }
    }
  }

  private def withRetry[T](body: => Future[T]): Future[T] = body recoverWith {
    case Upstream5xxResponse(_, 503, 503, _) => Thread.sleep(retryDelay); body
  }
}
