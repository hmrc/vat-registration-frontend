
package controllers.vatapplication

import itutil.ControllerISpec
import models.api.vatapplication.VatApplication
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, DateSelection}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.test.Helpers._

import java.time.LocalDate

class VoluntaryStartDateControllerISpec extends ControllerISpec {

  "GET /vat-start-date" must {
    "Return OK when the user is authenticated" in {
      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].contains(VatApplication(startDate = testApplicantIncorpDate))
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/vat-start-date").get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "Return INTERNAL_SERVER_ERROR when the user is not authenticated" in {
      given()
        .user.isNotAuthorised

      val res = buildClient("/vat-start-date").get()

      whenReady(res) { result =>
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /vat-start-date" must {

    "Redirect to the next page when all data is valid" in {
      val today = LocalDate.now().plusDays(1)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

      given()
        .user.isAuthorised()
        .s4lContainer[VatApplication].isUpdatedWith(VatApplication(startDate = Some(today)))
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/vat-start-date").post(Map(
        "value" -> DateSelection.specific_date.toString,
        "startDate.day" -> today.getDayOfMonth.toString,
        "startDate.month" -> today.getMonthValue.toString,
        "startDate.year" -> today.getYear.toString
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.vatapplication.routes.CurrentlyTradingController.show.url)
      }
    }
  }

}
