
package controllers.registration.applicant

import java.time.LocalDate

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.ScrsAddress
import models.external.{Name, Applicant}
import models.view._
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import repositories.SessionRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import controllers.registration.applicant.{routes => applicantRoutes}

class FormerNameControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

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

  val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

  "POST Former Name page" should {
    val s4lData = ApplicantDetails(
      incorporationDetails = Some(testIncorpDetails),
      transactorDetails = Some(testTransactorDetails),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "patch Applicant Details in backend without former name" in new Setup {
      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "${applicant.name.first}",
           |    "middle": "${applicant.name.middle}",
           |    "last": "${applicant.name.last}"
           |  },
           |  "role": "${applicant.role}",
           |  "dob": "$dob",
           |  "nino": "$nino",
           |  "currentAddress": {
           |    "line1": "$addrLine1",
           |    "line2": "$addrLine2",
           |    "postcode": "$postcode"
           |  },
           |  "contact": {
           |    "email": "$email",
           |    "tel": "1234",
           |    "mobile": "5678"
           |  }
           |}""".stripMargin)

      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[ApplicantDetails].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map("formerNameRadio" -> Seq("false")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.ContactDetailsController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
        (json \ "contact" \ "email").as[JsString].value mustBe email
        (json \ "contact" \ "tel").as[JsString].value mustBe "1234"
        (json \ "contact" \ "mobile").as[JsString].value mustBe "5678"
        (json \ "changeOfName").validateOpt[JsObject].get mustBe None
      }
    }

    "patch Applicant Details in backend with former name" in new Setup {
      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "${applicant.name.first}",
           |    "middle": "${applicant.name.middle}",
           |    "last": "${applicant.name.last}"
           |  },
           |  "role": "${applicant.role}",
           |  "dob": "$dob",
           |  "nino": "$nino",
           |  "currentAddress": {
           |    "line1": "$addrLine1",
           |    "line2": "$addrLine2",
           |    "postcode": "$postcode"
           |  },
           |  "contact": {
           |    "email": "$email",
           |    "tel": "1234",
           |    "mobile": "5678"
           |  },
           |  "changeOfName": {
           |    "name": {
           |      "first": "New",
           |      "middle": "Name",
           |      "last": "Cosmo"
           |    },
           |    "change": "2000-07-12"
           |  }
           |}""".stripMargin)

      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(formerName = Some(FormerNameView(false, None))))
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[ApplicantDetails].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "changeOfName" \ "change").as[LocalDate] mustBe LocalDate.of(2000, 7, 12)
        (json \ "changeOfName" \ "name" \ "first").as[JsString].value mustBe "New"
        (json \ "changeOfName" \ "name" \ "middle").as[JsString].value mustBe "Name"
        (json \ "changeOfName" \ "name" \ "last").as[JsString].value mustBe "Cosmo"
      }
    }

    "save Applicant Details to S4L if user needs to provide a former name date" in new Setup {
      val updatedS4LData = s4lData.copy(formerNameDate = None)

      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(formerName = Some(FormerNameView(false, None)), formerNameDate = None))
        .s4lContainer[ApplicantDetails].isUpdatedWith(updatedS4LData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show().url)
      }
    }
  }

}