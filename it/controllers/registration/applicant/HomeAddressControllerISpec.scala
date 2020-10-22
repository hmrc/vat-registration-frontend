
package controllers.registration.applicant

import java.time.LocalDate

import controllers.registration.applicant.{routes => applicantRoutes}
import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.TelephoneNumber
import models.api.{Address, Country}
import models.external.{Applicant, EmailAddress, EmailVerified, Name}
import models.view._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration, _}
import scala.concurrent.{Await, Future}

class HomeAddressControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

  class Setup {
    val repo = app.injector.instanceOf[SessionRepository]
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId: String): Boolean = {
      val preAwait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preAwait + 1
      res
    }
  }

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

  val currentAddress = Address(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))


  "GET redirectToAlf" should {
    val s4lData = ApplicantDetails(
      incorporationDetails = Some(testIncorpDetails),
      transactorDetails = Some(testTransactorDetails),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      emailAddress = Some(EmailAddress("test@t.test")),
      emailVerified = Some(EmailVerified(true)),
      telephoneNumber = Some(TelephoneNumber("1234")),
      formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .alfeJourney.initialisedSuccessfully()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.HomeAddressController.redirectToAlf().url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
  }

  "GET Txm ALF callback for Home Address" should {
    val s4lData = ApplicantDetails(
      incorporationDetails = Some(testIncorpDetails),
      transactorDetails = Some(testTransactorDetails),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      emailAddress = Some(EmailAddress("test@t.test")),
      emailVerified = Some(EmailVerified(true)),
      telephoneNumber = Some(TelephoneNumber("1234")),
      formerName = Some(FormerNameView(false, None)),
      formerNameDate = None,
      previousAddress = Some(PreviousAddressView(true, None))
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
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[ApplicantDetails].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.HomeAddressController.addressLookupCallback(id = addressId).url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PreviousAddressController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "currentAddress" \ "line1").as[JsString].value mustBe addressLine1
        (json \ "currentAddress" \ "line2").as[JsString].value mustBe addressLine2
        (json \ "currentAddress" \ "country").as[Country] mustBe Country(Some("GB"), Some("United Kingdom"))
        (json \ "currentAddress" \ "postcode").as[JsString].value mustBe addressPostcode
      }
    }
  }

}
