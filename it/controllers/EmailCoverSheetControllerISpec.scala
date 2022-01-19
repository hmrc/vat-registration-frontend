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

package controllers

import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.{AttachmentType, Attachments, IdentityEvidence, EmailMethod, VAT2}
import play.api.libs.json.{JsString, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class EmailCoverSheetControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.registration.attachments.routes.EmailCoverSheetController.show.url

  val testAckRef = "VRN1234567"

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised
        .vatScheme.has("acknowledgement-reference", JsString(s"$testAckRef"))
        .vatScheme.has("attachments", Json.toJson(Attachments(Some(EmailMethod), List[AttachmentType](IdentityEvidence, VAT2))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }
}
