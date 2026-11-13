/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.attachments

import common.enums.VatRegStatus
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import itutil.ControllerISpec
import models.CurrentProfile
import play.api.http.HeaderNames
import play.api.test.Helpers._

class CoverLetterPreviewControllerISpec extends ControllerISpec {

  val showUrl: String = routes.CoverLetterPreviewController.show.url

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  val profile: CurrentProfile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)

  s"GET $showUrl" must {
    "return an OK when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(profile, sessionString)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "redirect to the application submitted page when VrsNewAttachmentJourney is disabled" in new Setup {
      disable(VrsNewAttachmentJourney)
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(profile, sessionString)

      val res = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }
  }
}
