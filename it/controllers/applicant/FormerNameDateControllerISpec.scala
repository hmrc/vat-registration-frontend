/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.applicant

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.external.Name
import models.{ApplicantDetails, FormerName}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class FormerNameDateControllerISpec extends ControllerISpec {

  val url: String = routes.FormerNameDateController.show.url
  val testFormerName: FormerName = FormerName(
    hasFormerName = Some(true),
    name = Some(Name(Some(testFormerFirstName), last = testFormerLastName)),
    change = None
  )
  val testFormerNameDate: LocalDate = testApplicantDob.plusYears(1)
  val testApplicant: ApplicantDetails = validFullApplicantDetails.copy(changeOfName = testFormerName)

  s"GET $url" must {
    "return an OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return an OK with prepopulated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(changeOfName = testFormerName.copy(change = Some(testFormerNameDate)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("formerNameDate.day").attr("value") mustBe testFormerNameDate.getDayOfMonth.toString
        Jsoup.parse(res.body).getElementById("formerNameDate.month").attr("value") mustBe testFormerNameDate.getMonthValue.toString
        Jsoup.parse(res.body).getElementById("formerNameDate.year").attr("value") mustBe testFormerNameDate.getYear.toString
      }
    }

    "fail if the user is missing date of birth" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(personalDetails = None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }

    "redirect to the missing answer page if the user is missing former name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(changeOfName = FormerName())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }

    "redirect to the missing answer page if the user is missing date of birth" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(personalDetails = Some(testPersonalDetails.copy(dateOfBirth = None)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }
  }

  s"POST $url" must {
    "Update backend with former name date and redirect to the task list page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.replaceSection[ApplicantDetails](testApplicant.copy(changeOfName = testFormerName.copy(change = Some(testFormerNameDate))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(url).post(Map(
        "formerNameDate.day" -> testFormerNameDate.getDayOfMonth.toString,
        "formerNameDate.month" -> testFormerNameDate.getMonthValue.toString,
        "formerNameDate.year" -> testFormerNameDate.getYear.toString
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to the missing answer page if the user is missing former name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(changeOfName = FormerName())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(url).post(Map(
        "formerNameDate.day" -> "",
        "formerNameDate.month" -> "",
        "formerNameDate.year" -> ""
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }

    "redirect to the missing answer page if the user is missing date of birth" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant.copy(personalDetails = Some(testPersonalDetails.copy(dateOfBirth = None)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(url).post(Map(
        "formerNameDate.day" -> "",
        "formerNameDate.month" -> "",
        "formerNameDate.year" -> ""
      ))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.errors.routes.ErrorController.missingAnswer.url)
      }
    }

    "return form with errors for invalid name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(url).post(Map(
        "formerNameDate.day" -> "",
        "formerNameDate.month" -> "",
        "formerNameDate.year" -> ""
      ))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}