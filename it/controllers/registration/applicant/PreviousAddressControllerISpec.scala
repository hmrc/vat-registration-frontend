
package controllers.registration.applicant

import java.time.LocalDate

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.ScrsAddress
import models.external.{Applicant, Name}
import models.view.{ApplicantDetails, ContactDetailsView, FormerNameDateView, FormerNameView, HomeAddressView, PreviousAddressView}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import controllers.registration.applicant.{routes => applicantRoutes}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.duration.{Duration, FiniteDuration}

class PreviousAddressControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

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
    name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
    role = role
  )

  val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

  "POST Previous Address page" should {
    val s4lData = ApplicantDetails(
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = None
    )

    val validJson = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "First",
         |    "middle": "Middle",
         |    "last": "Last"
         |  },
         |  "role": "$role",
         |  "dob": "$dob",
         |  "nino": "$nino",
         |  "details": {
         |    "currentAddress": {
         |      "line1": "$addrLine1",
         |      "line2": "$addrLine2",
         |      "postcode": "$postcode"
         |    },
         |    "contact": {
         |      "email": "$email",
         |      "tel": "1234",
         |      "mobile": "5678"
         |    },
         |    "changeOfName": {
         |      "name": {
         |        "first": "New",
         |        "middle": "Name",
         |        "last": "Cosmo"
         |      },
         |      "change": "2000-07-12"
         |    }
         |  }
         |}""".stripMargin)

    "patch Applicant Details in backend" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[ApplicantDetails].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(applicantRoutes.PreviousAddressController.submit().url).post(Map("previousAddressQuestionRadio" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessContactDetailsController.showPPOB().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "details" \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "details" \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
        (json \ "details" \ "changeOfName" \ "change").as[LocalDate] mustBe LocalDate.of(2000, 7, 12)
        (json \ "details" \ "changeOfName" \ "name" \ "first").as[JsString].value mustBe "New"
        (json \ "details" \ "changeOfName" \ "name" \ "middle").as[JsString].value mustBe "Name"
        (json \ "details" \ "changeOfName" \ "name" \ "last").as[JsString].value mustBe "Cosmo"
        (json \ "details" \ "contact" \ "email").as[JsString].value mustBe email
        (json \ "details" \ "previousAddress").validateOpt[JsObject].get mustBe None
      }
    }
  }

  "GET Txm ALF callback for Previous Address" should {
    val s4lData = ApplicantDetails(
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(false, None)),
      formerNameDate = None,
      previousAddress = None
    )

    "patch Applicant Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "United Kingdom"
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
           |  "details": {
           |    "currentAddress": {
           |      "line1": "$addressLine1",
           |      "line2": "$addressLine2",
           |      "postcode": "$addressPostcode"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
           |      "tel": "1234",
           |      "mobile": "5678"
           |    },
           |    "previousAddress": {
           |      "line1": "$addressLine1",
           |      "line2": "$addressLine2",
           |      "postcode": "$addressPostcode",
           |      "country": "$addressCountry"
           |    }
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

      val response = buildClient(applicantRoutes.PreviousAddressController.addressLookupCallback(id = addressId).url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessContactDetailsController.showPPOB().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "previousAddress" \ "line1").as[JsString].value mustBe addressLine1
        (json \ "details" \ "previousAddress" \ "line2").as[JsString].value mustBe addressLine2
        (json \ "details" \ "previousAddress" \ "country").as[JsString].value mustBe addressCountry
        (json \ "details" \ "previousAddress" \ "postcode").as[JsString].value mustBe addressPostcode
      }
    }
  }

}
