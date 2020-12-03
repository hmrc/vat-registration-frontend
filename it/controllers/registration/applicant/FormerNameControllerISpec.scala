
package controllers.registration.applicant

import java.time.LocalDate

import controllers.registration.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models.TelephoneNumber
import models.api.Address
import models.external.{Applicant, EmailAddress, EmailVerified, Name}
import models.view._
import play.api.http.HeaderNames
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import play.api.test.Helpers._

class FormerNameControllerISpec extends ControllerISpec {

  val keyBlock = "applicant-details"
  val email = "test@t.test"
  val nino = "SR123456C"
  val role = "03"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val applicant = Applicant(
    name = Name(first = Some("First"), middle = Some("Middle"), last = "Last"),
    role = role
  )

  val currentAddress = Address(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"), addressValidated = true)

  "POST Former Name page" should {
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
           |    "emailVerified": true,
           |    "tel": "1234"
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
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.HomeAddressController.redirectToAlf().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
        (json \ "contact" \ "email").as[JsString].value mustBe email
        (json \ "contact" \ "tel").as[JsString].value mustBe "1234"
        (json \ "contact" \ "emailVerified").as[JsBoolean].value mustBe true
        (json \ "changeOfName").validateOpt[JsObject].get mustBe None
      }
    }

    "Update S4L with formerName and redirec to the Former Name Date page" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(formerName = Some(FormerNameView(false, None))))
        .s4lContainer[ApplicantDetails].isUpdatedWith(s4lData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show().url)
      }
    }

    "save Applicant Details to S4L if user needs to provide a former name date" in new Setup {
      val updatedS4LData = s4lData.copy(formerNameDate = None)

      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lData.copy(formerName = Some(FormerNameView(true, None)), formerNameDate = None))
        .s4lContainer[ApplicantDetails].isUpdatedWith(updatedS4LData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.FormerNameDateController.show().url)
      }
    }
  }

}