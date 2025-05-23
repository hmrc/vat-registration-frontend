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
import models.api.{EligibilitySubmissionData, NonUkNonEstablished, UkCompany}
import models.{ApplicantDetails, Business}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ConfirmTradingNameControllerISpec extends ControllerISpec {
  val companyName = "testCompanyName"
  val url: String = controllers.business.routes.ConfirmTradingNameController.show.url

  "show Trading Name page" should {
    "return OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return OK with prepopulated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(true))))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Yes"
      }
    }

    "redirect to the missing answer page when the company name isn't present" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(true))))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testApplicantIncorpDetails.copy(companyName = None)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

  "submit Trading Name page" should {
    "return SEE_OTHER with redirect to capture trading name page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection(businessDetails.copy(hasTradingName = Some(false)))
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(false))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("false")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.CaptureTradingNameController.show.url)
      }
    }

    "return SEE_OTHER with redirect to PPOB" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection(businessDetails.copy(hasTradingName = Some(true)))
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AddressCharacterLimitGuideController.show.url)
      }
    }

    "return SEE_OTHER with redirect to Address Character Limit Guide" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.replaceSection(businessDetails.copy(hasTradingName = Some(true)))
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.AddressCharacterLimitGuideController.show.url)
      }
    }

    "redirect to the missing answer page if the company name is missing and the request was invalid" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testApplicantIncorpDetails.copy(companyName = None)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.replaceSection(businessDetails.copy(hasTradingName = Some(true)))
        .registrationApi.getSection[Business](Some(businessDetails.copy(hasTradingName = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("dfsdfsd")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }

    "return BAD_REQUEST if nothing is selected" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post("")

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}