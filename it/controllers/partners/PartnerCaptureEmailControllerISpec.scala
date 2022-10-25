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

package controllers.partners

import itutil.ControllerISpec
import models.Entity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerCaptureEmailControllerISpec extends ControllerISpec {

  def url(index: Int): String = routes.PartnerCaptureEmailAddressController.show(index).url

  val email = "test@test.com"
  val invalidEmail = "test@@test.com"

  s"GET ${url(2)}" should {
    "show the view correctly" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url(2)).get)
      response.status mustBe OK
    }

    "return OK with pre-populated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, Some(email), None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("email-address").attr("value") mustBe email
      }
    }

    "redirect to Partner Entity Type page if entity is missing" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }

    "redirect to Partner Entity Type page if entity is missing business details" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(None, Individual, Some(false), None, None, Some(email), None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }
  }

  s"POST ${url(2)}" should {
    "update the backend and redirect to the Partner Summary page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))
        .registrationApi.replaceSection[Entity](Entity(Some(testSoleTrader), Individual, Some(false), None, None, Some(email), None), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(email))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerSummaryController.show.url)
    }

    "Return BAD_REQUEST if email is not provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(""))))

      response.status mustBe BAD_REQUEST
    }

    "Return BAD_REQUEST if invalid email provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(invalidEmail))))

      response.status mustBe BAD_REQUEST
    }
  }
}
