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

import java.time.LocalDate

import featureswitch.core.config.{FeatureSwitching, StubEmailVerification}
import it.fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.TelephoneNumber
import models.api.ScrsAddress
import models.external.{Applicant, EmailAddress, EmailVerified, Name}
import models.view.{ApplicantDetails, FormerNameDateView, FormerNameView, HomeAddressView, PreviousAddressView}
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.AppAndStubs

class CaptureEmailPasscodeControllerISpec extends IntegrationSpecBase
  with AppAndStubs
  with FeatureSwitching
  with IntegrationPatience
  with ITRegistrationFixtures {

  private val testEmail = "test@test.com"
  private val testPasscode = "123456"

  val s4lContents = ApplicantDetails(emailAddress = Some(EmailAddress(testEmail)))

  "GET /email-address-verification" should {
    "show the view correctly" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .s4lContainer[ApplicantDetails].contains(s4lContents)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/email-address-verification").get)

      res.status mustBe OK
    }
  }

  "POST /email-address-verification" when {
    "the feature switch is enabled" should {
      "verify the entered passcode against the stub and redirect to Email Verified page" in new StandardTestHelpers {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .s4lContainer[ApplicantDetails].isUpdatedWith(s4lContents.copy(emailVerified = Some(EmailVerified(true))))
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", CREATED, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.registration.applicant.routes.EmailAddressVerifiedController.show().url)
      }

      "return BAD_REQUEST for an incorrect passcode" in new StandardTestHelpers {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .s4lContainer[ApplicantDetails].contains(s4lContents)
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        stubPost("/email-verification/verify-passcode", NOT_FOUND, Json.obj("passcode" -> testPasscode).toString)

        val res: WSResponse = await(buildClient("/email-address-verification").post(Map("email-passcode" -> testPasscode)))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
