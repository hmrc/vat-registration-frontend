
package controllers.registration.transactor

import itutil.ControllerISpec
import models.api.{Address, Country, EligibilitySubmissionData}
import models.external.{Applicant, Name}
import models.{DeclarationCapacityAnswer, Director, TransactorDetails}
import play.api.http.HeaderNames
import play.api.test.Helpers._

import java.time.LocalDate

class TransactorHomeAddressControllerISpec extends ControllerISpec {

  val keyBlock = "transactor-details"
  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"
  val name = "Johnny Test"
  val telephone = "1234"

  val transactor = Applicant(
    name = Name(first = Some("First"), middle = Some("Middle"), last = "Last"),
    role = role
  )

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)


  "GET redirectToAlf" should {
    val s4lData = TransactorDetails(
      personalDetails = Some(testPersonalDetails),
      isPartOfOrganisation = Some(true),
      organisationName = Some(name),
      telephone = Some(telephone),
      email = Some(email),
      address = Some(Address(addrLine1, Some(addrLine2), None, None, None, Some(postcode))),
      declarationCapacity = Some(DeclarationCapacityAnswer(Director))
    )

    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].contains(s4lData)
        .alfeJourney.initialisedSuccessfully()
        .vatScheme.contains(emptyUkCompanyVatScheme)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(routes.TransactorHomeAddressController.redirectToAlf.url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
  }

  "GET Txm ALF callback for Home Address" should {
    val s4lData = TransactorDetails(
      personalDetails = Some(testPersonalDetails),
      isPartOfOrganisation = Some(true),
      organisationName = Some(name),
      telephone = Some(telephone),
      email = Some(email),
      address = Some(Address(addrLine1, Some(addrLine2), None, None, None, Some(postcode))),
      declarationCapacity = Some(DeclarationCapacityAnswer(Director))
    )

    "patch Transactor Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "GB"
      val addressPostcode = "BN3 1JU"

      given()
        .user.isAuthorised
        .s4lContainer[TransactorDetails].contains(s4lData)
        .s4lContainer[TransactorDetails].clearedByKey
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .registrationApi.replaceSection(
        TransactorDetails(address = Some(Address(addressLine1, Some(addressLine2), None, None, None, Some(addressPostcode), Some(Country(Some("GB"), Some("United Kingdom"))))))
      )
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(routes.TransactorHomeAddressController.addressLookupCallback(id = addressId).url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.TelephoneNumberController.show.url)
      }
    }
  }

}
