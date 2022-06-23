
package controllers.attachments

import featureswitch.core.config.UploadDocuments
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.test.Helpers._

class AttachmentMethodControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/attachment-method"
  val emptyAttachmentList = Attachments(None, List())
  val fullAttachmentList = Attachments(Some(Post), List(IdentityEvidence))

  "GET /register-for-vat/attachment-method" when {
    "the backend contains no attachment information" must {
      "return OK and render the page with a blank form" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.has("attachments", Json.toJson(emptyAttachmentList))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=email]").hasAttr("checked") mustBe false
      }
    }
    "the backend contains Post as the attachment method" must {
      "return OK and render the page with the Post option selected" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.has("attachments", Json.toJson(fullAttachmentList))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[value=email]").hasAttr("checked") mustBe false
      }
    }
    "the backend contains Email as the attachment method" must {
      "return OK and render the page with the Email option selected" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.has("attachments", Json.toJson(fullAttachmentList.copy(method = Some(EmailMethod))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=email]").hasAttr("checked") mustBe true
      }
    }
    "the backend contains Upload as the attachment method" must {
      "return OK and render the page with the Upload option selected" in new Setup {
        enable(UploadDocuments)
        given
          .user.isAuthorised()
          .vatScheme.has("attachments", Json.toJson(fullAttachmentList.copy(method = Some(Attached))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=email]").hasAttr("checked") mustBe false
        disable(UploadDocuments)
      }
    }
  }

  "POST /register-for-vat/attachment-method" when {
    "Upload is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        enable(UploadDocuments)
        given
          .user.isAuthorised()
          .vatScheme.storesAttachments(Attachments(Some(Attached), List()))
          .vatScheme.deleteAttachments

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj(
          "value" -> "2"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.fileupload.routes.UploadDocumentController.show.url)
        disable(UploadDocuments)
      }
    }
    "Post is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.storesAttachments(Attachments(Some(Post), List()))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj(
          "value" -> "3"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostController.show.url)
      }
    }
    "Email is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.storesAttachments(Attachments(Some(Post), List()))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj(
          "value" -> "email"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.EmailDocumentsController.show.url)
      }
    }
    "nothing is selected" must {
      "return BAD_REQUEST" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }

    "unsupported attachment method selection" must {
      "return BAD_REQUEST" in new Setup {
        given
          .user.isAuthorised()
          .vatScheme.storesAttachments(Attachments(Some(Other), List()))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).post(Json.obj()))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
