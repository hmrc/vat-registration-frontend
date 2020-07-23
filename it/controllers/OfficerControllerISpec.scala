/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.time.LocalDate

import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import models.api.ScrsAddress
import models.external.{CoHoRegisteredOfficeAddress, Name, Officer}
import models.view._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import repositories.ReactiveMongoRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class OfficerControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {
  val keyBlock = "officer-data"

  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"

  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val testAddress = ScrsAddress(line1 = addrLine1, line2 = addrLine2, postcode = Some(postcode))
  val dob = LocalDate.of(1998, 7, 12)

  val officer = Officer(
    name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
    role = role
  )

  val officer2 = Officer(
    name = Name(forename = Some("TestFirst"), otherForenames = Some("TestMiddle"), surname = "TestLast"),
    role = "Secretary"
  )

  val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

  class Setup {
    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
    val repo = new ReactiveMongoRepository(app.configuration, mongo)
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId : String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  "POST Former Name page" should {
    val jsonApplicantName = Json.obj("name" -> Json.obj("first" -> officer.name.forename, "middle" -> officer.name.otherForenames, "last" -> officer.name.surname))
    val s4lData = LodgingOfficer(
      securityQuestions = Some(SecurityQuestionsView(dob)),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "patch Lodging Officer in backend without former name" in new Setup {
      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "${officer.name.forename}",
           |    "middle": "${officer.name.otherForenames}",
           |    "last": "${officer.name.surname}"
           |  },
           |  "role": "${officer.role}",
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
           |    }
           |  }
           |}""".stripMargin)

      given()
        .user.isAuthorised
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .vatScheme.has(keyBlock, jsonApplicantName)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map("formerNameRadio" -> Seq("false")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showContactDetails().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "dob").as[LocalDate] mustBe dob
        (json \ "details" \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "details" \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "details" \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
        (json \ "details" \ "contact" \ "email").as[JsString].value mustBe email
        (json \ "details" \ "contact" \ "tel").as[JsString].value mustBe "1234"
        (json \ "details" \ "contact" \ "mobile").as[JsString].value mustBe "5678"
        (json \ "details" \ "changeOfName").validateOpt[JsObject].get mustBe None
      }
    }

    "patch Lodging Officer in backend with former name" in new Setup {
      val validJson = Json.parse(
        s"""
           |{
           |  "name": {
           |    "first": "${officer.name.forename}",
           |    "middle": "${officer.name.otherForenames}",
           |    "last": "${officer.name.surname}"
           |  },
           |  "role": "${officer.role}",
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

      given()
        .user.isAuthorised
        .s4lContainer[LodgingOfficer].contains(s4lData.copy(formerName = Some(FormerNameView(false, None))))
        .vatScheme.has(keyBlock, jsonApplicantName)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showFormerNameDate().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "changeOfName" \ "change").as[LocalDate] mustBe LocalDate.of(2000, 7, 12)
        (json \ "details" \ "changeOfName" \ "name" \ "first").as[JsString].value mustBe "New"
        (json \ "details" \ "changeOfName" \ "name" \ "middle").as[JsString].value mustBe "Name"
        (json \ "details" \ "changeOfName" \ "name" \ "last").as[JsString].value mustBe "Cosmo"
      }
    }

    "save Lodging Officer to S4L if user needs to provide a former name date" in new Setup {
      val updatedS4LData = s4lData.copy(formerNameDate = None)

      given()
        .user.isAuthorised
        .vatScheme.has(keyBlock, jsonApplicantName)
        .s4lContainer[LodgingOfficer].contains(s4lData.copy(formerName = Some(FormerNameView(false, None)), formerNameDate = None))
        .s4lContainer[LodgingOfficer].isUpdatedWith(updatedS4LData)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/changed-name").post(Map(
        "formerNameRadio" -> Seq("true"),
        "formerName" -> Seq("New Name Cosmo")
      ))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showFormerNameDate().url)
      }
    }
  }

  "POST Home Address page" should {
    val s4lData = LodgingOfficer(
      securityQuestions = Some(SecurityQuestionsView(dob)),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"),Some(email), Some("5678"))),
      formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
      formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
      previousAddress = Some(PreviousAddressView(true, None))
    )

    val roAddress = CoHoRegisteredOfficeAddress(
      premises = "Pizza",
      addressLine1 = "Line1",
      addressLine2 = None,
      locality = "TechCity",
      country = Some("Italy"),
      poBox = None,
      postalCode = None,
      region = None
    )

    val validJson = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "${officer.name.forename}",
         |    "middle": "${officer.name.otherForenames}",
         |    "last": "${officer.name.surname}"
         |  },
         |  "role": "${officer.role}",
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

    "patch Lodging Officer in backend" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .company.hasROAddress(roAddress)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.OfficerController.submitHomeAddress().url).post(Map("homeAddressRadio" -> Seq(currentAddress.id)))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showPreviousAddress().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "details" \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "details" \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
      }
    }
  }

  "GET Txm ALF callback for Home Address" should {
    val s4lData = LodgingOfficer(
      securityQuestions = Some(SecurityQuestionsView(dob)),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(false, None)),
      formerNameDate = None,
      previousAddress = Some(PreviousAddressView(true, None))
    )

    "patch Lodging Officer with ALF address in backend" in new Setup {
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
           |    }
           |  }
           |}""".stripMargin)

      given()
        .user.isAuthorised
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.OfficerController.acceptFromTxmHomeAddress(id = addressId).url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showPreviousAddress().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "currentAddress" \ "line1").as[JsString].value mustBe addressLine1
        (json \ "details" \ "currentAddress" \ "line2").as[JsString].value mustBe addressLine2
        (json \ "details" \ "currentAddress" \ "country").as[JsString].value mustBe addressCountry
        (json \ "details" \ "currentAddress" \ "postcode").as[JsString].value mustBe addressPostcode
      }
    }
  }

  "POST Previous Address page" should {
    val s4lData = LodgingOfficer(
      securityQuestions = Some(SecurityQuestionsView(dob)),
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

    "patch Lodging Officer in backend" in new Setup {
      given()
        .user.isAuthorised
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.OfficerController.submitPreviousAddress().url).post(Map("previousAddressQuestionRadio" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.BusinessContactDetailsController.showPPOB().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/$keyBlock")
        (json \ "details" \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "details" \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "details" \ "currentAddress" \ "postcode").validateOpt[String].get mustBe currentAddress.postcode
        (json \ "dob").as[LocalDate] mustBe dob
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
    val s4lData = LodgingOfficer(
      securityQuestions = Some(SecurityQuestionsView(dob)),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("1234"), Some(email), Some("5678"))),
      formerName = Some(FormerNameView(false, None)),
      formerNameDate = None,
      previousAddress = None
    )

    "patch Lodging Officer with ALF address in backend" in new Setup {
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
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .vatScheme.patched(keyBlock, validJson)
        .s4lContainer[LodgingOfficer].cleared
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(controllers.routes.OfficerController.acceptFromTxmPreviousAddress(id = addressId).url).get()
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
