
package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api.{Address, Country, EligibilitySubmissionData, UkCompany}
import models.external.{EmailAddress, EmailVerified, Name}
import models.view._
import models.{ApplicantDetails, Director, TelephoneNumber}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class HomeAddressControllerISpec extends ControllerISpec {

  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

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
      formerName = Some(Name(Some("New"), Some("Name"), "Cosmo")),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)(ApplicantDetails.s4LWrites)
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

      val testApplicantDetails: ApplicantDetails = s4lData.copy(homeAddress = Some(HomeAddressView(
        addressId,
        Some(Address(
          addressLine1,
          Some(addressLine2),
          postcode = Some(addressPostcode),
          country = Some(Country(Some(addressCountry), Some("United Kingdom"))),
          addressValidated = true
        ))
      )))

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(s4lData)(ApplicantDetails.s4LWrites)
        .s4lContainer[ApplicantDetails].clearedByKey
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .registrationApi.replaceSection[ApplicantDetails](testApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(applicantRoutes.HomeAddressController.addressLookupCallback(id = addressId).url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PreviousAddressController.show.url)
      }
    }
  }

}
