

package controllers.test

import config.FrontendAppConfig
import itutil.ControllerISpec
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.i18n.MessagesApi
import views.html.test.vat_submission_json

import scala.concurrent.Future

class RetrieveVatSubmissionControllerISpec extends ControllerISpec {

  val connectorUrl = s"/vatreg/test-only/submissions/$testRegId/submission-payload"
  val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val messages: Messages = messagesApi.asScala.preferred(Seq(Lang("en")))
  val url: String = routes.RetrieveVatSubmissionController.retrieveSubmissionJson.url

  val testJsonResponse: JsObject = Json.obj(
    "allWent" -> "well"
  )

  val view: vat_submission_json = app.injector.instanceOf[vat_submission_json]
  val viewHtml: String = view(Json.prettyPrint(testJsonResponse))(FakeRequest("GET", url), messages, appConfig).body.trim

  "GET /test-only/submission-payload" must {
    "return OK with the submission Json" in new Setup {
      given().user.isAuthorised

      insertCurrentProfileIntoDb(currentProfile, sessionId)
      stubGet(connectorUrl, OK, testJsonResponse.toString())

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        res.body.trim mustBe viewHtml
      }
    }
  }

}
