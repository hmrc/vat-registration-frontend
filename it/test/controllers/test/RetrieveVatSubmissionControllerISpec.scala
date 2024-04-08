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

package controllers.test

import config.FrontendAppConfig
import itutil.ControllerISpec
import play.api.i18n.{Lang, Messages}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.i18n.MessagesApi
import views.html.test.VatSubmissionJson

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

  val view: VatSubmissionJson = app.injector.instanceOf[VatSubmissionJson]
  val viewHtml: String = view(Json.prettyPrint(testJsonResponse))(FakeRequest("GET", url), messages, appConfig).body.trim

  "GET /test-only/submission-payload" must {
    "return OK with the submission Json" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)
      stubGet(connectorUrl, OK, testJsonResponse.toString())

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        res.body.trim mustBe viewHtml
      }
    }
  }

}
