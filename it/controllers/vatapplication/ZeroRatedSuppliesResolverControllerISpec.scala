
package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class ZeroRatedSuppliesResolverControllerISpec extends ControllerISpec {

  val url = "/resolve-zero-rated-turnover"

  "GET /resolve-zero-rated-supplies" when {
    "the user has entered £0 as their turnover estimate" must {
      "store £0 as the zero rated estimate and bypass the zero-rated supplies page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[VatApplication](VatApplication(turnoverEstimate = Some(0), zeroRatedSupplies = Some(0)))
          .registrationApi.getSection[VatApplication](Some(VatApplication(turnoverEstimate = Some(0), zeroRatedSupplies = Some(0))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.SellOrMoveNipController.show.url)
      }
    }
    "the user has entered a non-zero value for their turnover estimate" must {
      "redirect to the zero-rated supplies page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[VatApplication](Some(fullVatApplication))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesController.show.url)
      }
    }
    "the vat scheme doesn't contain turnover" must {
      "redirect to the missin answer page" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

}
