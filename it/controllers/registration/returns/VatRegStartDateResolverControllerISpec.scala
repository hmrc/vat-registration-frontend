
package controllers.registration.returns

import itutil.ControllerISpec
import models.api.{Partnership, Trust}
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

class VatRegStartDateResolverControllerISpec extends ControllerISpec {

  val url = "/resolve-vat-start-date"

  "GET /resolve-vat-start-date" when {
    "the user is voluntary" when {
      "GRS returns a date of incorporation for the business" must {
        "redirect to the voluntary start date (with date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
              eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold.copy(mandatoryRegistration = false))),
              applicantDetails = Some(validFullApplicantDetails)
            ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnsController.voluntaryStartPage.url)
        }
      }
      "GRS doesn't return a date of incorporation for a UK company" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
              eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(threshold = threshold.copy(mandatoryRegistration = false))),
              applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testApplicantIncorpDetails.copy(dateOfIncorporation = None))))
            ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
      "GRS doesn't return a date of incorporation for a Partnership" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
              eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership, threshold = threshold.copy(mandatoryRegistration = false))),
              applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testPartnership.copy(dateOfIncorporation = None))))
            ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
      "date of incorporation is not relevant for the user's party type" must {
        "redirect to the voluntary start date (no date choice) page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
              eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Trust, threshold = threshold.copy(mandatoryRegistration = false))),
              applicantDetails = Some(validFullApplicantDetails.copy(entity = Some(testMinorEntity)))
            ))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.VoluntaryStartDateNoChoiceController.show.url)
        }
      }
    }
    "the user is mandatory" must {
      "redirect to the mandatory start date page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData),
            applicantDetails = Some(validFullApplicantDetails)
          ))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ReturnsController.mandatoryStartPage.url)
      }
    }
  }

}
