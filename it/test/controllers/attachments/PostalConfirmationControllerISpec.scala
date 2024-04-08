/*
 * Copyright 2024 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock.{deleteRequestedFor, getRequestedFor, urlEqualTo, verify}
import itutil.ControllerISpec
import models.ApiKey
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.NO_CONTENT
import play.api.test.Helpers._

class PostalConfirmationControllerISpec extends ControllerISpec {

  val url = "/postal-confirmation"
  val fullAttachmentList = Attachments(Some(Post))
  val deleteAllUpscanDetailsUrl = s"/vatreg/1/upscan-file-details"

  "GET /register-for-vat/postal-confirmation" when {
    "the backend contains no attachment information" must {
      "return OK and render the page with a blank form" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
      }
    }
  }

  "POST /register-for-vat/postal-confirmation" when {
    "yes is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        given
          .user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        given
          .registrationApi.getSection[Attachments](Some(fullAttachmentList), currentProfile.registrationId)

        stubDelete(deleteAllUpscanDetailsUrl, NO_CONTENT, "true")

        val res = await(buildClient(url).post(Map(
          "value" -> "true"
        )))
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostController.show.url)
        verify(deleteRequestedFor(urlEqualTo(deleteAllUpscanDetailsUrl)))
        verify(getRequestedFor(urlEqualTo(s"/vatreg/registrations/${currentProfile.registrationId}/sections/${ApiKey[Attachments]}")))
      }
    }
    "Post is selected" when {
      "no is selected" must {
        "store the answer and redirect to the next page" in new Setup {
          given
            .user.isAuthorised()
          insertCurrentProfileIntoDb(currentProfile, sessionString)
          given
            .registrationApi.getSection[Attachments](Some(fullAttachmentList), currentProfile.registrationId)

          val res = await(buildClient(url).post(Map(
            "value" -> "false"
          )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
          verify(getRequestedFor(urlEqualTo(s"/vatreg/registrations/${currentProfile.registrationId}/sections/${ApiKey[Attachments]}")))
        }
      }

    }
  }

}
