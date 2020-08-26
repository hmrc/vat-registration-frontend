/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import java.time.LocalDate

import common.enums.VatRegStatus
import fixtures.LodgingOfficerFixtures
import models.CurrentProfile
import models.api.ScrsAddress
import models.external.Name
import models.view._
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class LodgingOfficerServiceSpec extends VatRegSpec with LodgingOfficerFixtures {
  override val testRegId = "testRegId"

  override implicit val currentProfile = CurrentProfile(testRegId, VatRegStatus.draft)

  val validFullLodgingOfficerNoFormerName = validFullLodgingOfficer.copy(
    formerName = Some(FormerNameView(false, None)),
    formerNameDate = None
  )

  val jsonPartialLodgingOfficer = Json.parse(
    s"""
       |{
       |  "name": {
       |    "first": "First",
       |    "middle": "Middle",
       |    "last": "Last"
       |  },
       |  "role": "Director",
       |  "dob": "1998-07-12",
       |  "nino": "SR123456Z"
       |}
       """.stripMargin)

  class Setup(s4lData: Option[LodgingOfficer] = None, backendData: Option[JsValue] = None) {
    val service = new LodgingOfficerService(
      mockVatRegistrationConnector,
      mockS4LService
    )

    when(mockS4LService.fetchAndGetNoAux[LodgingOfficer](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(s4lData))

    when(mockVatRegistrationConnector.getLodgingOfficer(any())(any())).thenReturn(Future.successful(backendData))

    when(mockS4LService.saveNoAux(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForS4LSave(t: LodgingOfficer = emptyLodgingOfficer) {
    val service: LodgingOfficerService = new LodgingOfficerService(
      mockVatRegistrationConnector,
      mockS4LService
    ) {
      override def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
        Future.successful(t)
      }
    }

    when(mockS4LService.saveNoAux(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  class SetupForBackendSave(t: LodgingOfficer = validPartialLodgingOfficer) {
    val service: LodgingOfficerService = new LodgingOfficerService(
      mockVatRegistrationConnector,
      mockS4LService
    ) {
      override def getLodgingOfficer(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[LodgingOfficer] = {
        Future.successful(t)
      }
    }

    when(mockVatRegistrationConnector.patchLodgingOfficer(any())(any(), any())).thenReturn(Future.successful(Json.toJson("""{}""")))

    when(mockS4LService.clear(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(HttpResponse(200)))
  }

  "Calling getLodgingOfficer" should {
    val jsonFullLodgingOfficerWithEmail = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "First",
         |    "middle": "Middle",
         |    "last": "Last"
         |  },
         |  "role": "Director",
         |  "dob": "1998-07-12",
         |  "nino": "SR123456Z",
         |  "details": {
         |    "currentAddress": {
         |      "line1": "TestLine1",
         |      "line2": "TestLine2",
         |      "postcode": "TE 1ST"
         |    },
         |    "contact": {
         |      "email": "test@t.test"
         |    }
         |  }
         |}
       """.stripMargin)

    val jsonFullLodgingOfficerNoEmail = Json.parse(
      s"""
         |{
         |  "name": {
         |    "first": "First",
         |    "middle": "Middle",
         |    "last": "Last"
         |  },
         |  "role": "Director",
         |  "dob": "1998-07-12",
         |  "nino": "SR123456Z",
         |  "details": {
         |    "currentAddress": {
         |      "line1": "TestLine1",
         |      "line2": "TestLine2",
         |      "postcode": "TE 1ST"
         |    },
         |    "contact": {
         |      "mobile": "1234567890"
         |    }
         |  }
         |}
       """.stripMargin)

    "return a default LodgingOfficer view model if nothing is in S4L & backend" in new Setup {
      service.getLodgingOfficer returns emptyLodgingOfficer
    }

    "return a partial LodgingOfficer view model from backend" in new Setup(None, Some(jsonPartialLodgingOfficer)) {
      val expected = LodgingOfficer(
        homeAddress = None,
        contactDetails = None,
        formerName = None,
        formerNameDate = None,
        previousAddress = None
      )
      service.getLodgingOfficer returns expected
    }

    "return a full LodgingOfficer view model from backend with an email" in new Setup(None, Some(jsonFullLodgingOfficerWithEmail)) {
      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val expected: LodgingOfficer = LodgingOfficer(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(None, Some("test@t.test"), None)),
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )
      service.getLodgingOfficer returns expected
    }

    "return a full LodgingOfficer view model from backend without an email" in new Setup(None, Some(jsonFullLodgingOfficerNoEmail)) {
      val currentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))
      val expected: LodgingOfficer = LodgingOfficer(
        homeAddress = Some(HomeAddressView(currentAddress.id, Some(currentAddress))),
        contactDetails = Some(ContactDetailsView(None, None, Some("1234567890"))),
        formerName = Some(FormerNameView(false, None)),
        formerNameDate = None,
        previousAddress = Some(PreviousAddressView(true, None))
      )
      service.getLodgingOfficer returns expected
    }
  }

  "Calling updateLodgingOfficer" should {
    "return a LodgingOfficer" when {

      "updating current address" that {
        val currentAddress = ScrsAddress(line1 = "Line1", line2 = "Line2", postcode = Some("PO BOX"))
        val officerHomeAddress = HomeAddressView(currentAddress.id, Some(currentAddress))

        "makes the block incomplete and save to S4L" in new SetupForS4LSave(validPartialLodgingOfficer) {
          val expected = validPartialLodgingOfficer.copy(homeAddress = Some(officerHomeAddress))

          service.saveLodgingOfficer(officerHomeAddress) returns expected
        }

        "makes the block complete and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(homeAddress = Some(officerHomeAddress))

          service.saveLodgingOfficer(officerHomeAddress) returns expected
        }
      }

      "updating officer contact" that {
        val officerContactDetails = ContactDetailsView(Some("tt@dd.uk"))

        "makes the block incomplete and save to S4L" in new SetupForS4LSave(validPartialLodgingOfficer) {
          val expected = validPartialLodgingOfficer.copy(contactDetails = Some(officerContactDetails))

          service.saveLodgingOfficer(officerContactDetails) returns expected
        }

        "makes the block complete and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(contactDetails = Some(officerContactDetails))

          service.saveLodgingOfficer(officerContactDetails) returns expected
        }
      }

      "updating officer former name" that {
        val formerNameFalse = FormerNameView(false, None)
        val formerNameTrue = FormerNameView(true, Some("New FormerName TADA"))

        "makes the block incomplete and save to S4L, model was previously incomplete" in new SetupForS4LSave(validPartialLodgingOfficer) {
          val expected = validPartialLodgingOfficer.copy(formerName = Some(formerNameFalse))

          service.saveLodgingOfficer(formerNameFalse) returns expected
        }

        "makes the block incomplete and save to S4L, model was previously complete no former name" in new SetupForS4LSave(validFullLodgingOfficerNoFormerName) {
          val formerName = FormerNameView(true, Some("New Name TADA"))
          val expected = validFullLodgingOfficerNoFormerName.copy(formerName = Some(formerName))

          service.saveLodgingOfficer(formerName) returns expected
        }

        "makes the block complete with no former name and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(formerName = Some(formerNameFalse))

          service.saveLodgingOfficer(formerNameFalse) returns expected
        }

        "makes the block complete with a former name and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(formerName = Some(formerNameTrue))

          service.saveLodgingOfficer(formerNameTrue) returns expected
        }
      }

      "updating officer former name date change" that {
        val formerNameDate = FormerNameDateView(LocalDate.of(2002, 5, 15))

        "makes the block incomplete and save to S4L" in new SetupForS4LSave(validPartialLodgingOfficer) {
          val expected = validPartialLodgingOfficer.copy(formerNameDate = Some(formerNameDate))

          service.saveLodgingOfficer(formerNameDate) returns expected
        }

        "makes the block complete and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(formerNameDate = Some(formerNameDate))

          service.saveLodgingOfficer(formerNameDate) returns expected
        }
      }

      "updating officer previous address" that {
        val addr = ScrsAddress(line1 = "PrevLine1", line2 = "PrevLine2", postcode = Some("PO PRE"))
        val previousAddress = PreviousAddressView(true, Some(addr))

        "makes the block incomplete and save to S4L" in new SetupForS4LSave(validPartialLodgingOfficer) {
          val expected = validPartialLodgingOfficer.copy(previousAddress = Some(previousAddress))

          service.saveLodgingOfficer(previousAddress) returns expected
        }

        "makes the block complete and save to backend" in new SetupForBackendSave(validFullLodgingOfficer) {
          val expected = validFullLodgingOfficer.copy(previousAddress = Some(previousAddress))

          service.saveLodgingOfficer(previousAddress) returns expected
        }
      }
    }
  }

}
