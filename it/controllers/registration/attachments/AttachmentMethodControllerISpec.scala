
package controllers.registration.attachments

import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api.{Attachments, EmailMethod, IdentityEvidence, Post}
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
        Jsoup.parse(res.body).select("input[id=post]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[id=email]").hasAttr("checked") mustBe false
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
        Jsoup.parse(res.body).select("input[id=post]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[id=email]").hasAttr("checked") mustBe false
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
        Jsoup.parse(res.body).select("input[id=post]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[id=email]").hasAttr("checked") mustBe true
      }
    }
  }

  "POST /register-for-vat/attachment-method" when {
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
  }

}
