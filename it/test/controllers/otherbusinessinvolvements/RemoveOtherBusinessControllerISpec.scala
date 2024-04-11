/*
 * Copyright 2022 HM Revenue & Customs
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
import models.OtherBusinessInvolvement
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class RemoveOtherBusinessControllerISpec extends ControllerISpec {
  val idx1: Int = 1
  val idx2: Int = 2

  def pageGetUrl(index: Int): String = routes.RemoveOtherBusinessController.show(index).url

  def pagePostUrl(index: Int): String = routes.RemoveOtherBusinessController.submit("testOtherBusinessName", index).url

  s"GET ${pageGetUrl(idx1)}" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageGetUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "redirect to minIdx page if given index is less than minIdx" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageGetUrl(0)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(pageGetUrl(idx1))
      }
    }

    "redirect to Obi Summary when Other Business Name is missing" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement.copy(businessName = None)), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageGetUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
      }
    }

    "redirect to Obi Summary when other business can't be found" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageGetUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
      }
    }
  }

  s"GET ${pageGetUrl(idx2)}" must {
    "return OK if it is a valid index" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement), idx = Some(idx2))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageGetUrl(idx2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST ${pagePostUrl(idx1)}" must {
    "return a redirect to summary page after deleting the Other Business if deleted was not the last one" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.deleteSection[OtherBusinessInvolvement](optIdx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(fullOtherBusinessInvolvement)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pagePostUrl(idx1)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
      }
    }

    "return a redirect to Other Business Involvement page after deleting the Other Business if the deleted was the last one" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.deleteSection[OtherBusinessInvolvement](optIdx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](Some(List.empty))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pagePostUrl(idx1)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessInvolvementController.show.url)
      }
    }

    "return a redirect to summary page when user chooses 'No'" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pagePostUrl(idx1)).post(Map("value" -> Seq("false")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ObiSummaryController.show.url)
      }
    }

    "return BAD_REQUEST if none of the option is selected" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pagePostUrl(idx1)).post(Map("value" -> Seq("")))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}