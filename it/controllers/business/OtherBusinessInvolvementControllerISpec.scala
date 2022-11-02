/*
 * Copyright 2017 HM Revenue & Customs
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

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NonUkNonEstablished}
import models.{Business, OtherBusinessInvolvement}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class OtherBusinessInvolvementControllerISpec extends ControllerISpec {

  val url: String = controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url

  s"GET $url" must {
    "return OK" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "return OK when there is an answer to prepop" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].contains(Business(otherBusinessInvolvement = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" must {
    "redirect to Imports or Exports if no other business involvement for UkCompany" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .s4lContainer[Business].isUpdatedWith(Business(otherBusinessInvolvement = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.deleteSection[OtherBusinessInvolvement]()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.vatapplication.routes.ImportsOrExportsController.show.url)
      }
    }

    "redirect to Turnover if no other business involvement for NonUkCompany" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .s4lContainer[Business].isUpdatedWith(Business(otherBusinessInvolvement = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))
        .registrationApi.deleteSection[OtherBusinessInvolvement]()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.vatapplication.routes.TurnoverEstimateController.show.url)
      }
    }

    "redirect to the task list page if TaskList FS is on" in new Setup {
      enable(TaskList)
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .s4lContainer[Business].isUpdatedWith(Business(otherBusinessInvolvement = Some(true)))
        .registrationApi.getSection[Business](None)
        .registrationApi.deleteSection[OtherBusinessInvolvement]()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.TaskListController.show.url)
      }
      disable(TaskList)
    }

    "redirect to other business involvement workflow" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .registrationApi.getSection[Business](None)
        .s4lContainer[Business].isUpdatedWith(Business(otherBusinessInvolvement = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1).url)
      }
    }

    "fail with bad request for submission without a selection" in new Setup {
      given
        .user.isAuthorised()
        .s4lContainer[Business].isEmpty
        .s4lContainer[Business].isUpdatedWith(Business(otherBusinessInvolvement = Some(true)))
        .registrationApi.getSection[Business](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res: Future[WSResponse] = buildClient(url).post("")

      whenReady(res) {
        _.status mustBe BAD_REQUEST
      }
    }
  }
}