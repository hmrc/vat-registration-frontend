
package controllers.registration.applicant

import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{Address, Country, EligibilitySubmissionData}
import models.external.{Applicant, EmailAddress, EmailVerified, Name}
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber}
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._

import java.time.LocalDate

class HomeAddressControllerISpec extends ControllerISpec {

  val keyBlock = "applicant-details"
  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val applicant = Applicant(
    name = Name(first = Some("First"), middle = Some("Middle"), last = "Last"),
    role = role
  )

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)


  "GET redirectToAlf" should {
    val s4lData = ApplicantDetails(
      entity = Some(testIncorpDetails),
      personalDetails = Some(testPersonalDetails),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      emailAddress = Some(EmailAddress("test@t.test")),
      emailVerified = Some(EmailVerified(true)),
      telephoneNumber = Some(TelephoneNumber("1234")),
      hasFormerName = Some(true),
      formerName = Some(Name(Some("New"), Some("Name"),"Cosmo")),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .alfeJourney.initialisedSuccessfully()
        .vatScheme.contains(emptyUkCompanyVatScheme)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.HomeAddressController.redirectToAlf.url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
  }

  "GET Txm ALF callback for Home Address" should {
    val s4lData = ApplicantDetails(
      entity = Some(testIncorpDetails),
      personalDetails = Some(testPersonalDetails),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      emailAddress = Some(EmailAddress("test@t.test")),
      emailVerified = Some(EmailVerified(true)),
      telephoneNumber = Some(TelephoneNumber("1234")),
      hasFormerName = Some(false),
      formerName = None,
      formerNameDate = None,
      previousAddress = Some(PreviousAddressView(true, None)),
      roleInTheBusiness = Some(Director)

    )

    "patch Applicant Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "GB"
      val addressPostcode = "BN3 1JU"

      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "First",
           |    "middle": "Middle",
           |    "last": "Last"
           |  },
           |  "role": "Director",
           |  "dob": "1998-07-12",
           |  "nino": "AA112233Z",
           |  "currentAddress": {
           |    "line1": "$addressLine1",
           |    "line2": "$addressLine2",
           |    "postcode": "$addressPostcode"
           |  },
           |  "contact": {
           |    "email": "$email",
           |    "emailVerified": true,
           |    "telephone": "1234"
           |  }
           |}""".stripMargin)

      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.HomeAddressController.addressLookupCallback(id = addressId).url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PreviousAddressController.show.url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "currentAddress" \ "line1").as[JsString].value mustBe addressLine1
        (json \ "currentAddress" \ "line2").as[JsString].value mustBe addressLine2
        (json \ "currentAddress" \ "country").as[Country] mustBe Country(Some("GB"), Some("United Kingdom"))
        (json \ "currentAddress" \ "postcode").as[JsString].value mustBe addressPostcode
      }
    }
  }

}
