
package controllers.returns

import itutil.ControllerISpec
import models.api.returns.Returns
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class ZeroRatedSuppliesResolverControllerISpec extends ControllerISpec {

  val url = "/resolve-zero-rated-turnover"

  "GET /resolve-zero-rated-supplies" when {
    "the user has entered £0 as their turnover estimate" must {
      "store £0 as the zero rated estimate and bypass the zero-rated supplies page" in new Setup {
        given
          .user.isAuthorised()
          .s4lContainer[Returns].contains(Returns(turnoverEstimate = Some(0)))
          .s4lContainer[Returns].isUpdatedWith(Returns(turnoverEstimate = Some(0), zeroRatedSupplies = Some(0)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }
    "the user has entered a non-zero value for their turnover estimate" must {
      "redirect to the zero-rated supplies page" in new Setup {
        given
          .s4lContainer[Returns].contains(Returns(turnoverEstimate = Some(1)))
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesController.show.url)
      }
    }
    "the vat scheme doesn't contain turnover" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        given
          .s4lContainer[Returns].contains(Returns(turnoverEstimate = None))
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
