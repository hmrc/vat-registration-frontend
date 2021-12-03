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

package controllers.registration.attachments

import featureswitch.core.config.EmailAttachments
import itutil.ControllerISpec
import models.api.{Attachments, Post}
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class Vat2RequiredControllerISpec extends ControllerISpec{

  val url: String = controllers.registration.attachments.routes.Vat2RequiredController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }

  s"POST $url" when {
    "the EmailAttachments feature switch is enabled" must {
      "redirect to the AttachmentMethod page" in {
        enable(EmailAttachments)
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val res = buildClient(url).post(Json.obj())

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.AttachmentMethodController.show.url)
        }
      }
    }
    "the EmailAttachments feature switch is disabled" must {
      "redirect to the Document Post page" in {
        disable(EmailAttachments)
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .vatScheme.storesAttachments(Attachments(method = Some(Post), List()))

        val res = buildClient(url).post(Json.obj())

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostController.show.url)
        }
      }
    }
  }
}
