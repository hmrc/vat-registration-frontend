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

package controllers

import java.time.LocalDate

import common.enums.VatRegStatus
import itutil.ControllerISpec
import play.api.test.Helpers._
import utils.VATRegFeatureSwitches

class WelcomeControllerISpec extends ControllerISpec {

  def controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])

  val featureSwitch: VATRegFeatureSwitches = app.injector.instanceOf[VATRegFeatureSwitches]
  val thresholdUrl = s"/vatreg/threshold/${LocalDate.now()}"
  val currentThreshold = "50000"

  "WelcomeController" must {
    "return an OK status" when {
      "user is authenticated and authorised to access the app without profile" in new Setup {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists()
          .vatScheme.regStatus(VatRegStatus.draft.toString)
          .audit.writesAuditMerged()
          .vatRegistration.threshold(thresholdUrl, currentThreshold)

        whenReady(controller.show(request))(res => res.header.status mustBe SEE_OTHER)
      }
    }

    "return an OK status" when {
      "user is authenticated and authorised to access the app with profile" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAuditMerged()
          .vatRegistration.threshold(thresholdUrl, currentThreshold)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        whenReady(controller.show(request))(res => res.header.status mustBe SEE_OTHER)
      }
    }
  }
}
