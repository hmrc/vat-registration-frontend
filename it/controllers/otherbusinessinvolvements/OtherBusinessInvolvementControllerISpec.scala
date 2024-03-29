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

package controllers.otherbusinessinvolvements

import itutil.ControllerISpec
import models.{Business, OtherBusinessInvolvement}
import models.api.EligibilitySubmissionData
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
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "return OK when there is an answer to prepop" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(Business(otherBusinessInvolvement = Some(true))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).get()

      whenReady(res) { result =>
        result.status mustBe OK
        Jsoup.parse(result.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }
  }

  s"POST $url" must {
    "redirect to the task list page after deleting old OBI answers if the answer is no" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.deleteSection[OtherBusinessInvolvement]()
        .registrationApi.replaceSection[Business](Business(otherBusinessInvolvement = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "false"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to other business involvement if the answer is yes" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](Business(otherBusinessInvolvement = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map("value" -> "true"))

      whenReady(res) { result =>
        result.status mustBe SEE_OTHER
        result.headers(HeaderNames.LOCATION) must contain(controllers.otherbusinessinvolvements.routes.OtherBusinessNameController.show(1).url)
      }
    }

    "fail with bad request for submission without a selection" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post("")

      whenReady(res) {
        _.status mustBe BAD_REQUEST
      }
    }
  }
}