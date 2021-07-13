package uk.gov.hmrc.organisationsdetailsapi.services

import java.util.UUID

import org.joda.time.Interval
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.organisationsdetailsapi.domain.NinoMatch

import scala.concurrent.Future

trait CorporationTaxService {

  def resolve(matchId: UUID)(implicit hc: HeaderCarrier): Future[NinoMatch]

  def get(matchId: UUID, interval: Interval, endpoint: String, scopes: Iterable[String])
          (implicit hc: HeaderCarrier, request: RequestHeader): Future[Seq[Employment]]
}

class SandboxCorporationTaxService {

}

class LiveCorporationTaxService {

}
