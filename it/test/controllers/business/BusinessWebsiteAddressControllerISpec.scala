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

class BusinessWebsiteAddressControllerISpec extends ControllerISpec {

  val url: String = controllers.business.routes.BusinessWebsiteAddressController.show.url
  val businessWebsiteAddress = "https://www.example.com"
  val invalidWebsiteAddress = "https://www.example.com/"
  val validWebsiteAddress = "https://www.example.com/photos/"

  val data: Business = Business(website = Some(businessWebsiteAddress))

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url).get)
      response.status mustBe OK
    }

    "return OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(businessDetails.copy(website = Some(businessWebsiteAddress))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("businessWebsiteAddress").attr("value") mustBe businessWebsiteAddress
      }
    }
  }

  s"POST $url" when {
    "BusinessContact is not complete" should {
      "Update backend and redirect to the next page" in new Setup {

        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.replaceSection[Business](businessDetails.copy(website = Some(businessWebsiteAddress)))
          .registrationApi.getSection[Business](Some(businessDetails.copy(website = Some(businessWebsiteAddress))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(businessWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
      }
    }

    "BusinessContact is complete" should {
      "Post the block to the backend and redirect to vat correspondence page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Business](businessDetails.copy(website = Some(businessWebsiteAddress)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Business](Some(businessDetails.copy(website = Some(businessWebsiteAddress))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(businessWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
      }

      "Post the block to the backend and redirect to the next page and remove trailing slashes when necessary" in new Setup {

        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Business](businessDetails.copy(website = Some(businessWebsiteAddress)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Business](Some(businessDetails.copy(website = Some(businessWebsiteAddress))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(invalidWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
      }

      "Post the block to the backend and redirect to the next page and not remove trailing slashes when not necessary" in new Setup {

        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Business](businessDetails.copy(website = Some(validWebsiteAddress)))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[Business](Some(businessDetails.copy(website = Some(validWebsiteAddress))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(validWebsiteAddress))))

        response.status mustBe SEE_OTHER
        response.header("LOCATION") mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
      }
    }

    "return 400 when the user submits and invalid regex" in new Setup {
      val invalidUrl: String = businessWebsiteAddress + "@"

      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[Business](businessDetails.copy(website = Some(invalidUrl)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url).post(Map("businessWebsiteAddress" -> Seq(invalidUrl))))

      response.status mustBe BAD_REQUEST
    }
  }
}
