/*
 * Copyright 2024 HM Revenue & Customs
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
import models.Business
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessEmailControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.BusinessEmailController.show.url
  val businessEmail = "test@test.com"
  val invalidBusinessEmail = "test@@test.com"

  val data: Business = Business(email = Some(businessEmail))

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(businessDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url).get)
      response.status mustBe OK
    }

    "return OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(businessDetails.copy(email = Some(businessEmail))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("businessEmailAddress").attr("value") mustBe businessEmail
      }
    }
  }

  s"POST $url" should {
    "update BusinessContact and redirect to the next page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](businessDetails.copy(email = Some(businessEmail)))
        .registrationApi.getSection[Business](Some(businessDetails.copy(email = Some(businessEmail))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url).post(Map("businessEmailAddress" -> Seq(businessEmail))))

      response.status mustBe SEE_OTHER
      response.header("LOCATION") mustBe Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)
    }

    "Return BAD_REQUEST if invalid email provided" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url).post(Map("businessEmailAddress" -> Seq(invalidBusinessEmail))))

      response.status mustBe BAD_REQUEST
    }
  }
}
