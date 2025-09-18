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

package controllers.business

import forms.ContactPreferenceForm
import itutil.ControllerISpec
import models.{Business, Email, Letter}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

class ContactPreferenceControllerISpec extends ControllerISpec {

  val url: String = routes.ContactPreferenceController.showContactPreference.url

  s"GET $url" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").toArray() mustBe Nil
      }
    }
    "return OK with 'Email' pre-populated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(businessDetails.copy(contactPreference = Some(Email))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Email"
      }
    }
    "return OK with 'Letter' pre-populated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](Some(businessDetails.copy(contactPreference = Some(Letter))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Letter"
      }
    }
  }

  s"POST $url" must {
    "redirect to task list page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(contactPreference = Some(Letter)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> ContactPreferenceForm.letter)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}