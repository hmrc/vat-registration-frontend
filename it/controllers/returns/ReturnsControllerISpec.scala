
package controllers.returns

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Threshold, UkCompany}
import models.api.returns.Returns
import models.{ApplicantDetails, DateSelection}
import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import play.api.test.Helpers._

class ReturnsControllerISpec extends ControllerISpec {

  "GET /vat-start-date" must {
    "Return OK when the user is authenticated" in {
      given()
        .user.isAuthorised()
        .s4lContainer[Returns].contains(Returns(None, None, None, None, testApplicantIncorpDate))
        .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)(ApplicantDetails.s4LWrites)
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))
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
        .s4lContainer[Returns].isUpdatedWith(Returns(None, None, None, None, Some(java.time.LocalDate.parse(today.toString))))
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .vatScheme.has("threshold-data", Json.toJson(Threshold(mandatoryRegistration = false)))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      val res = buildClient("/vat-start-date").post(Json.obj(
        "value" -> DateSelection.specific_date,
        "startDate" -> Json.obj(
          "day" -> today.getDayOfMonth.toString,
          "month" -> today.getMonthOfYear.toString,
          "year" -> today.getYear.toString
        )
      ))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
      }
    }
  }

}
