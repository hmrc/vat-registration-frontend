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

import features.businessContact.models.BusinessContact
import helpers.RequestsFinder
import it.fixtures.ITRegistrationFixtures
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.JsString
import support.AppAndStubs

class BusinessContactDetailsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with ITRegistrationFixtures {

  "show PPOB" should {
    "return 200 when S4l returns view model" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.contains(vatReg)
        .company.hasROAddress(coHoRegisteredOfficeAddress)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities").get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
    "return 500 when not authorised" in {
      given()
        .user.isNotAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities").get()
      whenReady(response) { res =>
        res.status mustBe 500

      }
    }
  }
  "submit PPOB" should {
    "return 303 to Address Lookup frontend" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .company.hasROAddress(coHoRegisteredOfficeAddress)
        .alfeJourney.initialisedSuccessfully()

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities").post(Map("ppobRadio" -> Seq("other")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
    "return 303 to company contact details page (full model)" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.contains(vatReg)
        .company.hasROAddress(coHoRegisteredOfficeAddress)
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].cleared

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities").post(Map("ppobRadio" -> Seq("line1XXXX")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(features.businessContact.routes.BusinessContactDetailsController.showCompanyContactDetails().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/business-contact")
      json mustBe validBusinessContactDetailsJson

      }
    }
    "return 500 when model is complete and vat reg returns 500 (s4l is not cleared)" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.contains(vatReg)
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isNotUpdatedWith[BusinessContact](validBusinessContactDetails)

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities").post(Map("ppobRadio" -> Seq("line1XXXX")))
      whenReady(response) { res =>
        res.status mustBe 500
      }
    }
  }

"returnFromTxm GET" should {
    "return 303 save to vat as model is complete" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", scrsAddress.line1, scrsAddress.line2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].cleared
      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities/acceptFromTxm?id=fudgesicle").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(features.businessContact.routes.BusinessContactDetailsController.showCompanyContactDetails().url)

        val json = getPATCHRequestJsonBody(s"/vatreg/1/business-contact")
        json mustBe validBusinessContactDetailsJson
      }

    }
    "returnFromTxm should return 303 save to s4l as model is incomplete" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .address("fudgesicle", scrsAddress.line1, scrsAddress.line2, "UK", "XX XX").isFound
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")
        .s4lContainer[BusinessContact].isUpdatedWith(validBusinessContactDetails.copy(companyContactDetails = None))

      val response = buildClient("/where-will-company-carry-out-most-of-its-business-activities/acceptFromTxm?id=fudgesicle").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(features.businessContact.routes.BusinessContactDetailsController.showCompanyContactDetails().url)
      }
    }
  }
  "showCompanyContactDetails" should {
    "return 200" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)

      val response = buildClient("/company-contact-details").get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

  }
  "submitCompanyContactDetails" should {
    "return 303 and submit to s4l because the model is incomplete" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("foo@foo.com"), "phoneNumber" -> Seq("0121401")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show().url)

      }
    }
    "return 303 and submit to vat reg because the model is complete" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(companyContactDetails = None))
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].cleared

      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "phoneNumber" -> Seq("123"), "mobileNumber" -> Seq("987654"), "websiteAddress" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicAndCompliance.routes.BusinessActivityDescriptionController.show().url)
      }
    }
    "return 404 when vat returns a 404" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .vatScheme.doesNotExistForKey("business-contact")
      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "phoneNumber" -> Seq("123"), "mobileNumber" -> Seq("987654"), "websiteAddress" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 404
      }
    }
    "return 500 when update to vat reg returns an error (s4l is not cleared)" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isNotUpdatedWith[BusinessContact](validBusinessContactDetails)
      val response = buildClient("/company-contact-details").post(Map("email" -> Seq("test@foo.com"), "phoneNumber" -> Seq("123"), "mobileNumber" -> Seq("987654"), "websiteAddress" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe 500
      }
    }
  }
}