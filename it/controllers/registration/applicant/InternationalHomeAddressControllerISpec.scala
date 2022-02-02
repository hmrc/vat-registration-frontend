
package controllers.registration.applicant

import itutil.ControllerISpec
import models.{ApplicantDetails, TransactorDetails}
import models.api.{Address, Country, EligibilitySubmissionData}
import models.view.HomeAddressView
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class InternationalHomeAddressControllerISpec extends ControllerISpec {

  val url = "/home-address/international"
  val testForeignCountry = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress = address.copy(country = Some(testForeignCountry))

  "GET /home-address/international" when {
    "reading from S4L" must {
      "return OK when the ApplicantDetails block is empty" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
      }
      "return OK and pre-populate when the ApplicantDetails block contains an address" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails(homeAddress = Some(HomeAddressView("", Some(testShortForeignAddress)))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
    "when reading from the backend" must {
      "return OK and pre-populate the page" in new Setup {
        val appDetails = ApplicantDetails(homeAddress = Some(HomeAddressView("", Some(testForeignAddress))))
        val vatScheme = emptyUkCompanyVatScheme.copy(applicantDetails = Some(appDetails))
        given
          .user.isAuthorised()
          .s4lContainer[ApplicantDetails].isEmpty
          .vatScheme.contains(vatScheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .vatScheme.has("applicant-details", Json.toJson(appDetails)(ApplicantDetails.writes))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
  }

  "POST /home-address/international" must {
    "Store the address and redirect to the previous address page if a minimal address is provided" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.doesNotExistForKey("applicant-details")
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(homeAddress = Some(HomeAddressView("", Some(testShortForeignAddress)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.show.url)
    }
    "Store the address and redirect to the previous address page if a full address is provided" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.doesNotExistForKey("applicant-details")
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(homeAddress = Some(HomeAddressView("", Some(testForeignAddress)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.show.url)
    }
    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.doesNotExistForKey("applicant-details")
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(homeAddress = Some(HomeAddressView("", Some(testForeignAddress)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.doesNotExistForKey("applicant-details")
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(homeAddress = Some(HomeAddressView("", Some(testForeignAddress)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is UK and postcode is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .vatScheme.doesNotExistForKey("applicant-details")
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(homeAddress = Some(HomeAddressView("", Some(testForeignAddress)))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "country" -> "United Kingdom"
      )))

      res.status mustBe BAD_REQUEST
    }
  }

}
