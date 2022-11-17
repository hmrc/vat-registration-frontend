
package controllers.applicant

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.api._
import models.external.{EmailAddress, EmailVerified, Name}
import models.view.{FormerNameDateView, HomeAddressView, PreviousAddressView}
import models.{ApplicantDetails, Director, TelephoneNumber, TransactorDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class PreviousAddressControllerISpec extends ControllerISpec {

  val email = "test@t.test"
  val nino = "SR123456C"
  val role = "03"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)

  val pageUrl: String = routes.PreviousAddressController.show.url

  val applicantDetails = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber("1234")),
    hasFormerName = Some(true),
    formerName = Some(Name(Some("New"), Some("Name"), "Cosmo")),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = None,
    roleInTheBusiness = Some(Director)
  )

  s"GET $pageUrl" must {
    "redirect user back to the Current Address route if address missing" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(applicantDetails.copy(homeAddress = None)), testRegId, None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.HomeAddressController.redirectToAlf.url)
    }

    "render the page for a transactor" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(applicantDetails)(ApplicantDetails.s4LWrites)
        .registrationApi.getSection[ApplicantDetails](None, testRegId)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe false
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe false
    }

    "render the page with prepop" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(applicantDetails.copy(previousAddress = Some(PreviousAddressView(yesNo = false, Some(address))))), testRegId, None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe false
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
  }

  s"POST $pageUrl" must {
    "redirect to International Address capture if the user is a NETP" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(NETP)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(applicantDetails), testRegId, None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(pageUrl)
        .post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.InternationalPreviousAddressController.show.url)
    }

    "redirect to International Address capture if the user is a Non UK Company" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(applicantDetails), testRegId, None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(pageUrl)
        .post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.InternationalPreviousAddressController.show.url)
    }

    "patch Applicant Details in backend when no previous address" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].contains(applicantDetails)(ApplicantDetails.s4LWrites)
        .registrationApi.replaceSection(applicantDetails.copy(previousAddress = Some(PreviousAddressView(yesNo = true, None))))
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.PreviousAddressController.submit.url).post(Map("value" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

  "GET Txm ALF callback for Previous Address" must {
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
      previousAddress = None,
      roleInTheBusiness = Some(Director)
    )

    "patch Applicant Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "GB"
      val addressPostcode = "BN3 1JU"

      val testApplicantDetails: ApplicantDetails = s4lData.copy(previousAddress = Some(PreviousAddressView(
        yesNo = true,
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

      val response: Future[WSResponse] = buildClient(applicantRoutes.PreviousAddressController.addressLookupCallback(id = addressId).url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

}
