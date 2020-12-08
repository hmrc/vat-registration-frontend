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
import itutil.IntegrationSpecBase
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.AppAndStubs

class EmailVerifiedControllerISpec extends IntegrationSpecBase with AppAndStubs with FeatureSwitching with IntegrationPatience {

  "GET /email-address-verified" should {
    "show the view correctly" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: WSResponse = await(buildClient("/email-address-verified").get)

      res.status mustBe OK
    }
  }

  "POST /email-address-verification" should {
      "return NotImplemented" in new StandardTestHelpers {
        disable(StubEmailVerification)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient("/email-address-verified").post(""))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.CaptureTelephoneNumberController.show().url)
      }
    }

}
