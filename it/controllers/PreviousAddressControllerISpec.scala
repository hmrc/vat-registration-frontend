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

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import features.officer.models.view._
import helpers.RequestsFinder
import it.fixtures.VatRegistrationFixture
import models.S4LVatLodgingOfficer
import models.api.{CompletionCapacity, Name, ScrsAddress, VatLodgingOfficer}
import models.external.Officer
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsBoolean, JsString, Json}
import support.AppAndStubs

class PreviousAddressControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with VatRegistrationFixture{
  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"

  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val testAddress = ScrsAddress(line1 = addrLine1, line2 = addrLine2, postcode = Some(postcode))
  val dob = LocalDate.of(1998, 7, 12)

  val s4LVatLodgingOfficer = S4LVatLodgingOfficer(
    officerHomeAddress = Some(HomeAddressView("12345", Some(testAddress))),
    officerSecurityQuestions = Some(SecurityQuestionsView(dob, nino, None)),
    completionCapacity = Some(CompletionCapacityView(CompletionCapacity(
      Name(surname = "Bobble", forename = Some("Jingles"), otherForenames = None),
      role))),
    officerContactDetails = Some(ContactDetailsView(email = Some(email), daytimePhone = None, mobile = None)),
    formerName = Some(FormerNameView(false)),
    formerNameDate = None,
    previousAddress = None
  )

  "POST Previous Address page" should {
    val officer = Officer(
      name = Name(forename = Some("First"), otherForenames = Some("Middle"), surname = "Last"),
      role = role
    )
    val updatedS4LVatLodgingOfficer = s4LVatLodgingOfficer.copy(previousAddress = Some(PreviousAddressView(true)))

    val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
    val s4lData = LodgingOfficer(
      completionCapacity = Some(CompletionCapacityView(officer.name.id, Some(officer))),
      securityQuestions = Some(SecurityQuestionsView(dob, nino)),
      homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
      contactDetails = Some(ContactDetailsView(Some("test@t.test"), Some("1234"), Some("5678"))),
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
         |      "line1": "TestLine1",
         |      "line2": "TestLine2",
         |      "postcode": "TE 1ST"
         |    },
         |    "contact": {
         |      "email": "test@t.test",
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

    "upsert Lodging Officer in backend" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .s4lContainer[LodgingOfficer].contains(s4lData)
        .vatScheme.patched("lodgingOfficer", validJson)
        .s4lContainer.cleared
        .audit.writesAudit()

      val response = buildClient("/current-address-three-years-or-more").post(Map("previousAddressQuestionRadio" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatContact.ppob.routes.PpobController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/officer")
        (json \ "currentAddress" \ "line1").as[JsString].value mustBe currentAddress.line1
        (json \ "currentAddress" \ "line2").as[JsString].value mustBe currentAddress.line2
        (json \ "currentAddress" \ "postcode").as[JsString].value mustBe currentAddress.postcode
        (json \ "dob").as[LocalDate] mustBe dob
        (json \ "nino").as[JsString].value mustBe nino
        (json \ "role").as[JsString].value mustBe role
        (json \ "name" \ "surname").as[JsString].value mustBe "Bobble"
        (json \ "name" \ "forename").as[JsString].value mustBe "Jingles"
        (json \ "changeOfName" \ "nameHasChanged").as[JsBoolean].value mustBe false
        (json \ "currentOrPreviousAddress" \ "currentAddressThreeYears").as[JsBoolean].value mustBe true
        (json \ "contact" \ "email").as[JsString].value mustBe email
      }
    }
  }

  "GET Txm Address Lookup callback" should {
    "upsert Vat Lodging Officer with ALF address in backend" in {
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
           |      "line1": "TestLine1",
           |      "line2": "TestLine2",
           |      "postcode": "TE 1ST"
           |    },
           |    "contact": {
           |      "email": "test@t.test",
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
           |    },
           |    "previousAddress": {
           |      "line1": "TestLine11",
           |      "line2": "TestLine22",
           |      "postcode": "TE1 1ST"
           |    }
           |  }
           |}""".stripMargin)

      val address = ScrsAddress(
        line1 = addressLine1,
        line2 = addressLine2,
        country = Some(addressCountry),
        postcode = Some(addressPostcode)
      )

      val updatedS4LVatLodgingOfficer = s4LVatLodgingOfficer.copy(previousAddress = Some(PreviousAddressView(false, Some(address))))

      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(s4LVatLodgingOfficer, Some(STARTED))
        .s4lContainerInScenario[S4LVatLodgingOfficer].isUpdatedWith(updatedS4LVatLodgingOfficer, Some(STARTED), Some("Vat Lodging Officer updated"))
        .vatScheme.isBlank
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(updatedS4LVatLodgingOfficer, Some("Vat Lodging Officer updated"))
        .vatScheme.isUpdatedWith[VatLodgingOfficer](S4LVatLodgingOfficer.apiT.toApi(updatedS4LVatLodgingOfficer))
        .audit.writesAudit()

      val response = buildClient(s"/current-address-three-years-or-more/acceptFromTxm?id=$addressId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatContact.ppob.routes.PpobController.show().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/lodging-officer")
        (json \ "currentOrPreviousAddress" \ "currentAddressThreeYears").as[JsBoolean].value mustBe false
        (json \ "currentOrPreviousAddress" \ "previousAddress" \ "line1").as[JsString].value mustBe "16 Coniston court"
        (json \ "currentOrPreviousAddress" \ "previousAddress" \ "line2").as[JsString].value mustBe "Holland road"
        (json \ "currentOrPreviousAddress" \ "previousAddress" \ "country").as[JsString].value mustBe "United Kingdom"
        (json \ "currentOrPreviousAddress" \ "previousAddress" \ "postcode").as[JsString].value mustBe "BN3 1JU"
      }
    }
  }
}
