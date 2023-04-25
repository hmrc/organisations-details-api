package unit.uk.gov.hmrc.organisationsdetailsapi.controllers

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import utils.TestSupport

class VatReturnDetailsControllerSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with TestSupport
    with BeforeAndAfterEach {
  implicit val sys: ActorSystem = ActorSystem("MyTest")

}
