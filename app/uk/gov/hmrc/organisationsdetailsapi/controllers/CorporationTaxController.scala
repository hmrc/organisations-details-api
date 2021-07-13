package uk.gov.hmrc.organisationsdetailsapi.controllers

import javax.inject.Inject
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.organisationsdetailsapi.errorhandler.ErrorHandling
import uk.gov.hmrc.organisationsdetailsapi.services.DetailsService

class CorporationTaxController @Inject()(val authConnector: AuthConnector,
                                         cc: ControllerComponents,
                                         detailsService: DetailsService) extends BaseApiController(cc) with ErrorHandling {

}
