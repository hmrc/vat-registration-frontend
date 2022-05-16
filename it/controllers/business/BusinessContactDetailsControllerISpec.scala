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

package controllers.business

import itutil.ControllerISpec
import models.{BusinessContact, CompanyContactDetails}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.test.Helpers._

class BusinessContactDetailsControllerISpec extends ControllerISpec {

  "show" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
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
        .user.isAuthorised()
        .s4lContainer[BusinessContact].isEmpty
        .s4lContainer[BusinessContact].isUpdatedWith(BusinessContact())
        .registrationApi.getSection[BusinessContact](None, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("foo@foo.com"), "daytimePhone" -> Seq("0121401890")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
      }
    }
    "return SEE_OTHER and submit to vat reg because the model is complete" in new Setup {
      val updateBusinessContact = validBusinessContactDetails.copy(companyContactDetails = Some(CompanyContactDetails(
        email = "test@foo.com",
        phoneNumber = Some("1234567890"),
        mobileNumber = Some("9876547890"),
        website = Some("/test/url")
      )))

      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails.copy(companyContactDetails = None))
        .registrationApi.replaceSection[BusinessContact](updateBusinessContact, testRegId)(BusinessContact.apiKey, BusinessContact.apiFormat)
        .s4lContainer[BusinessContact].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
      }
    }
    "return NOT_FOUND when vat returns a 404" ignore new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].isEmpty
        .registrationApi.getSectionFails[BusinessContact]()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe NOT_FOUND
      }
    }
    "return INTERNAL_SERVER_ERROR when update to vat reg returns an error (s4l is not cleared)" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BusinessContact].contains(validBusinessContactDetails)
        .registrationApi.replaceSectionFails[BusinessContact](testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/business-contact-details").post(Map("email" -> Seq("test@foo.com"), "daytimePhone" -> Seq("1234567890"), "mobile" -> Seq("9876547890"), "website" -> Seq("/test/url")))
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}