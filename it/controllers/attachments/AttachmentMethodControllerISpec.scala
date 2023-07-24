
package controllers.attachments

import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import models.external.upscan.{InProgress, UpscanDetails}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._

class AttachmentMethodControllerISpec extends ControllerISpec with ITRegistrationFixtures {

  val url = "/attachment-method"
  val fullAttachmentList = Attachments(Some(Post))

  "GET /register-for-vat/attachment-method" when {
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
    "the backend contains Post as the attachment method" must {
      "return OK and render the page with the Post option selected" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[Attachments](Some(fullAttachmentList))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe false
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe true
      }
    }
    "the backend contains Upload as the attachment method" must {
      "return OK and render the page with the Upload option selected" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[Attachments](Some(fullAttachmentList.copy(method = Some(Attached))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK
        Jsoup.parse(res.body).select("input[value=2]").hasAttr("checked") mustBe true
        Jsoup.parse(res.body).select("input[value=3]").hasAttr("checked") mustBe false
      }
    }
  }

  "POST /register-for-vat/attachment-method" when {
    "Upload is selected" must {
      "store the answer and redirect to the next page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Attached)))
          .upscanApi.deleteAttachments()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(Map(
          "value" -> "2"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.fileupload.routes.UploadDocumentController.show.url)
      }
    }
    "Post is selected" when {
      "no upscan details are present" must {
      "store the answer and redirect to the next page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Post)))
        given
          .user.isAuthorised()
          .registrationApi.replaceSection[Attachments](Attachments(Some(Attached)))
          .upscanApi.fetchAllUpscanDetails(List())
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(Map(
          "value" -> "3"
        )))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostController.show.url)
      }
    }
      "upscan is in progress" must {
        "Redirect to the error page" in new Setup {
          given
            .user.isAuthorised()
            .registrationApi.replaceSection[Attachments](Attachments(Some(Post)))
          given
            .user.isAuthorised()
            .registrationApi.replaceSection[Attachments](Attachments(Some(Attached)))
            .upscanApi.fetchAllUpscanDetails(List(UpscanDetails(VAT51, "ref", None, InProgress, None)))
          insertCurrentProfileIntoDb(currentProfile, sessionString)

          val res = await(buildClient(url).post(Map(
            "value" -> "3"
          )))

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.DocumentsPostErrorController.show.url)
        }
      }
  }
    "nothing is selected" must {
      "return BAD_REQUEST" in new Setup {
        given
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(""))

        res.status mustBe BAD_REQUEST
      }
    }
  }

}
