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

import featureswitch.core.config.{FeatureSwitching, StubEmailVerification}
import fixtures.ApplicantDetailsFixture
import itutil.IntegrationSpecBase
import models.TelephoneNumber
import models.external.EmailAddress
import models.view.ApplicantDetails
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.AppAndStubs

class CaptureTelephoneNumberControllerISpec extends IntegrationSpecBase
  with AppAndStubs
  with FeatureSwitching
  with IntegrationPatience
  with ApplicantDetailsFixture {

  private val testPhoneNumber = "12345 123456"

  "GET /telephone-number" should {
    "show the view correctly" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/telephone-number").get)

      res.status mustBe OK

    }
  }

  "POST /telephone-number" when {
    val keyblock = "applicant-details"
    "the ApplicantDetails model is incomplete" should {
      "update S4L and redirect to ALF to capture the PPOB address" in new StandardTestHelpers {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails().copy(telephoneNumber = Some(TelephoneNumber(testPhoneNumber))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.routes.BusinessContactDetailsController.ppobRedirectToAlf().url)
      }
    }
    "the ApplicantDetails model is complete" should {
      "post to the backend and redirect to ALF to capture the PPOB address" in new StandardTestHelpers {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(validFullApplicantDetails)
          .vatScheme.patched(keyblock, Json.toJson(validFullApplicantDetails)(ApplicantDetails.apiFormat))
          .s4lContainer[ApplicantDetails].cleared

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient("/telephone-number").post(Map("telephone-number" -> Seq(testPhoneNumber))))

        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.routes.BusinessContactDetailsController.ppobRedirectToAlf().url)
      }
    }
  }

}