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

package controllers.registration.applicant

import featureswitch.core.config.StubEmailVerification
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NETP, NonUkNonEstablished}
import models.external.{EmailAddress, EmailVerified}
import models.{ApplicantDetails, Director, TelephoneNumber}
import org.jsoup.Jsoup
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureTelephoneNumberControllerISpec extends ControllerISpec {

  private val testPhoneNumber = "12345 123456"

  val url: String = controllers.registration.applicant.routes.CaptureTelephoneNumberController.show.url

  val s4lData = ApplicantDetails(
    entity = Some(testIncorpDetails),
    personalDetails = Some(testPersonalDetails),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber(testPhoneNumber)),
    roleInTheBusiness = Some(Director)
  )

  val keyblock = "applicant-details"

  s"GET $url" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient(url).get)

      res.status mustBe OK

    }

    "returns an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .s4lContainer[ApplicantDetails].contains(s4lData)
        .audit.writesAuditMerged()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById("telephone-number").attr("value") mustBe testPhoneNumber
      }
    }
  }

  s"POST $url" when {
    "the ApplicantDetails model is incomplete" should {
      "update S4L and redirect to ALF to capture the PPOB address" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(telephoneNumber = Some(TelephoneNumber(testPhoneNumber))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.business.routes.PpobAddressController.startJourney.url)
      }
    }

    "update S4L and redirect to International Address for a NETP" in new Setup {
      disable(StubEmailVerification)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(telephoneNumber = Some(TelephoneNumber(testPhoneNumber))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.registration.business.routes.InternationalPpobAddressController.show.url)
    }

    "update S4L and redirect to International Address for a Non UK Company" in new Setup {
      disable(StubEmailVerification)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
        .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(telephoneNumber = Some(TelephoneNumber(testPhoneNumber))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.registration.business.routes.InternationalPpobAddressController.show.url)
    }

    "the ApplicantDetails model is complete" should {
      "post to the backend and redirect to ALF to capture the PPOB address" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.business.routes.PpobAddressController.startJourney.url)
      }
      "post to the backend and redirect to International Address for a NETP" in new Setup {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.writes))
          .s4lContainer[ApplicantDetails].clearedByKey
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.business.routes.InternationalPpobAddressController.show.url)
      }
    }
  }

}
