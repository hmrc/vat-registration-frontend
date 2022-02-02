
package controllers.registration.returns

import itutil.ControllerISpec
import models.TurnoverEstimates
import models.api.returns.Returns
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import support.RegistrationsApiStubs

class ZeroRatedSuppliesResolverControllerISpec extends ControllerISpec with RegistrationsApiStubs {

  val url = "/resolve-zero-rated-turnover"

  "GET /resolve-zero-rated-supplies" when {
    "the user has entered £0 as their turnover estimate" must {
      "store £0 as the zero rated estimate and bypass the zero-rated supplies page" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(estimates = TurnoverEstimates(0))
        ))

        given
          .user.isAuthorised()
          .s4lContainer[Returns].isUpdatedWith(Returns(zeroRatedSupplies = Some(0)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(scheme)))

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ClaimRefundsController.show.url)
      }
    }
    "the user has entered a non-zero value for their turnover estimate" must {
      "redirect to the zero-rated supplies page" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(estimates = TurnoverEstimates(1))
          ))

        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)
        specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(scheme)))

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ZeroRatedSuppliesController.show.url)
      }
    }
    "the vat scheme doesn't contain eligibility data" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(eligibilitySubmissionData = None)

        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)
        specificRegistrationApi(testRegId).GET.respondsWith(OK, Some(Json.toJson(scheme)))

        val res = await(buildClient(url).get)

        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
