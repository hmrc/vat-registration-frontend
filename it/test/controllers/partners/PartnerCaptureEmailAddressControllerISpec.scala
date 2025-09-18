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

import config.FrontendAppConfig
import itutil.ControllerISpec
import models.Entity
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerCaptureEmailAddressControllerISpec extends ControllerISpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(2)).get())
      response.status mustBe OK
    }

    "return OK with pre-populated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, Some(email), None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }

    "redirect to task list controller if no entities available" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect back to partner entity type page if requested index is less than min allowed index" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(1)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerCaptureEmailAddressController.show(PartnerIndexValidation.minPartnerIndex).url)
      }
    }

    "redirect back to partner entity type page if requested index is greater than max allowed index" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(100)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerCaptureEmailAddressController.show(appConfig.maxPartnerCount).url)
      }
    }

    "redirect back to partner entity type page if requested index is greater than available partners plus 1" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(4)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerCaptureEmailAddressController.show(3).url)
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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(email))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerSummaryController.show.url)
    }

    "redirect back to partner email address page if submitted with index less than configured min allowed partner count" in new Setup {
      val requestedIndex = 1
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(requestedIndex)).post(Map("email-address" -> Seq(email))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerCaptureEmailAddressController.show(PartnerIndexValidation.minPartnerIndex).url)
    }

    "redirect back to partner email address page if submitted with index greater than configured max allowed partner count" in new Setup {
      val requestedIndex = 100
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(requestedIndex)).post(Map("email-address" -> Seq(email))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerCaptureEmailAddressController.show(appConfig.maxPartnerCount).url)
    }

    "Return BAD_REQUEST if email is not provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(""))))

      response.status mustBe BAD_REQUEST
    }

    "Return BAD_REQUEST if invalid email provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: WSResponse = await(buildClient(url(2)).post(Map("email-address" -> Seq(invalidEmail))))

      response.status mustBe BAD_REQUEST
    }
  }
}
