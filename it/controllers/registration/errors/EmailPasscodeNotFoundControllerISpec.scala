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

package controllers.registration.errors

import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import play.api.test.Helpers._

class EmailPasscodeNotFoundControllerISpec extends ControllerISpec {

  "show" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(routes.EmailPasscodeNotFoundController.show("test").url).get())

      res.status mustBe OK
    }
  }

}
