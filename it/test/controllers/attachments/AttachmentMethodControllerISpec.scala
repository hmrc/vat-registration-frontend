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

import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import itutil.ControllerISpec
import models.api._
import models.external.upscan.{InProgress, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class AttachmentMethodControllerISpec extends ControllerISpec {

  val url = "/attachment-method"
  val fullAttachmentList: Attachments = Attachments(Some(Post))

  "GET /register-for-vat/attachment-method" when {
    "the backend contains no attachment information" must {
      "return OK and render the page with a blank form" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
      }
    }
    "the backend contains Post as the attachment method" must {
      "return OK and render the page with the Post option selected" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Attachments](Some(fullAttachmentList))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe true
      }
    }
    "the backend contains Upload as the attachment method" must {
      "return OK and render the page with the Upload option selected" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Attachments](Some(fullAttachmentList.copy(method = Some(Upload))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
      }
    }
  }

  "POST /register-for-vat/attachment-method" when {
    "Upload is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        enable(VrsNewAttachmentJourney)
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Upload)))
          .upscanApi.deleteAttachments()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(
          "value" -> "2"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.fileupload.routes.UploadSummaryController.show.url)
      }
    }
    "Post is selected" when {
      "no upscan details are present" must {
      "store the answer and redirect to the next page" in new Setup {
        enable(VrsNewAttachmentJourney)
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Post)))
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Upload)))
          .upscanApi.fetchAllUpscanDetails(List())
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(Map(
          "value" -> "3"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PostalConfirmationController.show.url)
      }
    }
      "upscan is in progress" must {
        "Redirect to the error page" in new Setup {
          given()
            .user.isAuthorised()
            .registrationApi.replaceSection[Attachments](Attachments(Some(Post)))
          given()
            .user.isAuthorised()
            .registrationApi.replaceSection[Attachments](Attachments(Some(Upload)))
            .upscanApi.fetchAllUpscanDetails(List(UpscanDetails(VAT51, "ref", None, InProgress, None)))
          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res: WSResponse = await(buildClient(url).post(Map(
            "value" -> "3"
          )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostErrorController.show.url)
        }
      }
  }
    "nothing is selected" must {
      "return BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).post(""))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
