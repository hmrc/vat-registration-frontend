/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.errors

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, UkCompany}
import models.{ApplicantDetails, Contact, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class EmailConfirmationCodeMaxAttemptsExceededControllerISpec extends ControllerISpec {

  "show" must {
    "return an OK with a view that contains applicant email in the message when in applicant flow" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).getElementsByTag("p").text() contains validFullApplicantDetails.contact.email.get
    }

    "return an OK with a view that contains transactor email in the message when in transactor flow" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url).get())

      res.status mustBe OK
      Jsoup.parse(res.body).getElementsByTag("p").text() contains Some(validTransactorDetails.email)
    }

    "return INTERNAL_SERVER_ERROR when email is not present" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      val transactorDetailsWithoutEmail: TransactorDetails = validTransactorDetails.copy(email = None)
      val applicantDetailsWithoutEmail: ApplicantDetails = validFullApplicantDetails.copy(contact = Contact())
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(transactorDetailsWithoutEmail))
        .registrationApi.getSection[ApplicantDetails](Some(applicantDetailsWithoutEmail))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(routes.EmailConfirmationCodeMaxAttemptsExceededController.show.url).get())

      res.status mustBe INTERNAL_SERVER_ERROR
    }
  }

}
