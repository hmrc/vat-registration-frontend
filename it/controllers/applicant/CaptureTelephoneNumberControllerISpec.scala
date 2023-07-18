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

package controllers.applicant

import featuretoggle.FeatureSwitch.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, Contact}
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureTelephoneNumberControllerISpec extends ControllerISpec {

  private val testPhoneNumber = "12345 123456"
  private val testTrimmedPhoneNumber = "12345123456"

  val url: String = controllers.applicant.routes.CaptureTelephoneNumberController.show.url

  s"GET $url" must {
    "show the view correctly" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with prepopulated data" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(contact = Contact(tel = Some(testPhoneNumber)))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("telephone-number").attr("value") mustBe testPhoneNumber
      }
    }
  }

  s"POST $url" when {
    "post to the backend and redirect to Task List" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(contact = Contact())))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(
        contact = Contact(tel = Some(testTrimmedPhoneNumber))
      ))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.routes.TaskListController.show.url)
    }
    "return form with errors for an invalid phone number" in new Setup {
      disable(StubEmailVerification)
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(contact = Contact())))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq())))

      res.status mustBe BAD_REQUEST
    }
  }

}
