/*
 * Copyright 2018 HM Revenue & Customs
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

package features.businessContact

import features.businessContact.models.{BusinessContact, CompanyContactDetails}
import helpers.VatRegSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import _root_.models.api.ScrsAddress

import scala.concurrent.Future

class BusinessContactServiceSpec extends VatRegSpec {

  val testService = new BusinessContactService {
    override val registrationConnector  = mockRegConnector
    override val s4lConnector           = mockS4LConnector
  }

  "getBusinessContact" should {
    "return a populated BusinessContact model" when {
      "there is a model in S4L" in {
        val businessContact = BusinessContact(
          ppobAddress = Some(scrsAddress),
          companyContactDetails = Some(CompanyContactDetails("test@test.com", None, None, None))
        )

        mockS4LFetchAndGet[BusinessContact]("business-contact", Some(businessContact))

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }

      "there is data in the backend" in {
        val businessContact = BusinessContact(
          ppobAddress = Some(ScrsAddress(
            line1    = "testLine1",
            line2    = "testLine2",
            line3    = Some("testLine3"),
            line4    = Some("testLine4"),
            postcode = Some("TE57 7ET")
          )),
          companyContactDetails = Some(CompanyContactDetails(
            email          = "test@test.com",
            phoneNumber    = Some("0123456"),
            mobileNumber   = Some("987654"),
            websiteAddress = Some("/test/url")
          ))
        )

        val businessContactJson = Json.parse(
          s"""
            |{
            | "digitalContact" : {
            |   "email" : "test@test.com",
            |   "tel" : "0123456",
            |   "mobile" : "987654"
            | },
            | "website" : "/test/url",
            | "ppob" : {
            |   "line1" : "testLine1",
            |   "line2" : "testLine2",
            |   "line3" : "testLine3",
            |   "line4" : "testLine4",
            |   "postcode" : "TE57 7ET"
            | }
            |}
          """.stripMargin
        )

        mockS4LFetchAndGet[BusinessContact]("business-contact", None)

        when(mockRegConnector.getBusinessContact(any(), any()))
          .thenReturn(Future.successful(Some(businessContactJson)))

        mockS4LSave[BusinessContact]("business-contact")

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }
    }

    "return an empty model" when {
      "there is no data in either S4L or the backend" in {
        val businessContact = BusinessContact(
          ppobAddress           = None,
          companyContactDetails = None
        )

        mockS4LFetchAndGet[BusinessContact]("business-contact", None)

        when(mockRegConnector.getBusinessContact(any(), any()))
          .thenReturn(Future.successful(None))

        mockS4LSave[BusinessContact]("business-contact")

        val result = await(testService.getBusinessContact)
        result mustBe businessContact
      }
    }
  }

  "updateBusinessContact" should {
    "determine that the model is incomplete and save in S4L" in {
      val businessContact = BusinessContact(
        ppobAddress           = None,
        companyContactDetails = None
      )

      mockS4LFetchAndGet[BusinessContact]("business-contact", Some(businessContact))

      mockS4LSave[BusinessContact]("business-contact")

      val result = await(testService.updateBusinessContact[ScrsAddress](scrsAddress))
      result mustBe scrsAddress
    }

    "determine that the model is complete and save in the backend" in {
      val businessContact = BusinessContact(
        ppobAddress           = Some(scrsAddress),
        companyContactDetails = None
      )

      val companyContactDetails = CompanyContactDetails("test@test.com", None, None, None)

      mockS4LFetchAndGet[BusinessContact]("business-contact", Some(businessContact))

      when(mockRegConnector.upsertBusinessContact(any())(any(), any(), any()))
        .thenReturn(Future.successful(Json.parse("""{"abc" : "xyz"}""")))

      mockS4LClear()

      val result = await(testService.updateBusinessContact[CompanyContactDetails](companyContactDetails))
      result mustBe companyContactDetails
    }
  }
}
