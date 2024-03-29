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

import forms.ShortOrgNameForm
import itutil.ControllerISpec
import models.Business
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ShortOrgNameControllerISpec extends ControllerISpec {
  val testShortOrgName = "testShortOrgName"

  "show Short Org Name page" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/short-organisation-name").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return OK with prepop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(businessDetails.copy(shortOrgName = Some(testShortOrgName))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/short-organisation-name").get()

      whenReady(response) { res =>
        res.status mustBe OK
        res.body.contains(testShortOrgName) mustBe true
      }
    }
  }

  "submit Trading Name page" should {
    "return SEE_OTHER" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection(businessDetails.copy(shortOrgName = Some(testShortOrgName)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(businessDetails.copy(shortOrgName = Some(testShortOrgName))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/short-organisation-name").post(Map(ShortOrgNameForm.shortOrgNameKey -> Seq(testShortOrgName)))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ConfirmTradingNameController.show.url)
      }
    }

    "return BAD_REQUEST for missing trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/short-organisation-name").post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/short-organisation-name").post(Map(ShortOrgNameForm.shortOrgNameKey -> "a" * 161))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}