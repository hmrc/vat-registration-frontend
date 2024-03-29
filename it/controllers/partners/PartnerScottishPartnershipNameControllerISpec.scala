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
import forms.ScottishPartnershipNameForm
import itutil.ControllerISpec
import models.Entity
import models.api.{EligibilitySubmissionData, Individual, ScotPartnership, UkCompany}
import org.jsoup.Jsoup
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class PartnerScottishPartnershipNameControllerISpec extends ControllerISpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  def url(index: Int): String = routes.PartnerScottishPartnershipNameController.show(index).url

  s"GET ${url(2)}" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return OK with pre-pop for existing data" in new Setup {
      private val scottishPartnershipName = "updated name"

      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), ScotPartnership, Some(false), Some(scottishPartnershipName), None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("scottishPartnershipName").attr("value") mustBe scottishPartnershipName
      }
    }

    "redirect to tasklist if lead partner is missing" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(Nil))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
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
          Some(routes.PartnerScottishPartnershipNameController.show(PartnerIndexValidation.minPartnerIndex).url)
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
          Some(routes.PartnerScottishPartnershipNameController.show(appConfig.maxPartnerCount).url)
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
          Some(routes.PartnerScottishPartnershipNameController.show(3).url)
      }
    }
  }

  s"POST ${url(2)}" must {
    "update the backend and redirect to partner Partnership id" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None, None, None, None)), idx = Some(2))
        .registrationApi.replaceSection[Entity](Entity(None, ScotPartnership, Some(true), Some(testCompanyName), None, None, None), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2))
        .post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> testCompanyName))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerPartnershipIdController.startJourney(2).url)
      }
    }

    "redirect back to partnership name page if submitted with an invalid index" in new Setup {
      def validate(requestedIndex: Int, redirectIndex: Int): Assertion = {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None, None, None, None)), idx = Some(2))
          .registrationApi.replaceSection[Entity](Entity(None, ScotPartnership, Some(true), Some(testCompanyName), None, None, None), idx = Some(2))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url(requestedIndex))
          .post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> testCompanyName))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerScottishPartnershipNameController.show(redirectIndex).url)
        }
      }

      validate(1, 2)
      validate(100, appConfig.maxPartnerCount)
    }

    "return BAD_REQUEST for missing partnership name" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2))
        .post("")

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid partnership name" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url(2))
        .post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> "a" * 106))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
