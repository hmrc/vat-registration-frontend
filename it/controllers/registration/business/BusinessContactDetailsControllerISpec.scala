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

package controllers.registration.business

import itutil.ControllerISpec
import models.BusinessContact
import play.api.http.HeaderNames
import play.api.test.Helpers._

class BusinessContactDetailsControllerISpec extends ControllerISpec {

  "show" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

  }
  "submitCompanyContactDetails" should {
    "return SEE_OTHER and submit to s4l because the model is incomplete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .vatScheme.doesNotHave("business-contact")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("foo@foo.com"), "daytimePhone" -> Seq("0121401890")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ContactPreferenceController.showContactPreference().url)

      }
    }
    "return SEE_OTHER and submit to vat reg because the model is complete" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(companyContactDetails = None))
        .vatScheme.isUpdatedWith(validBusinessContactDetails)
        .s4lContainer[BusinessContact].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ContactPreferenceController.showContactPreference().url)
      }
    }
    "return NOT_FOUND when vat returns a 404" ignore new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].isEmpty
        .vatScheme.doesNotExistForKey("business-contact")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe NOT_FOUND
      }
    }
    "return INTERNAL_SERVER_ERROR when update to vat reg returns an error (s4l is not cleared)" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .vatScheme.isNotUpdatedWith[BusinessContact](validBusinessContactDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}