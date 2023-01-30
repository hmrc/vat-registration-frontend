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
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class PartnerEntityTypeControllerISpec extends ControllerISpec {

  implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val url: Int => String = (idx: Int) =>  controllers.partners.routes.PartnerEntityTypeController.showPartnerType(idx).url

  s"GET $url" should {
    "display the page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(Entity(None, ScotPartnership, Some(true), None, None, None, None))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "display the page with pre-pop" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, ScotPartnership, Some(true), None, None, None, None),
          Entity(None, UkCompany, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }

    "display the page with pre-pop for non-business party type" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(None, UkCompany, Some(false), None, None, None, None),
          Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 1
      }
    }

    "redirect to task list controller if no entities available" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

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

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(1)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerEntityTypeController.showPartnerType(PartnerIndexValidation.minPartnerIndex).url)
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

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(100)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerEntityTypeController.showPartnerType(appConfig.maxPartnerCount).url)
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

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(4)).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe
          Some(routes.PartnerEntityTypeController.showPartnerType(3).url)
      }
    }
  }

  s"POST $url" when {
    s"the user selects a individual person party type" should {
      "store the partyType in backend and begin a STI journey" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))
          .registrationApi.replaceSection(Entity(None, Individual, Some(false), None, None, None, None), idx = Some(partnerIndex))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> PartyType.stati(Individual))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerSoleTraderIdController.startJourney(partnerIndex).url)
      }
    }

    s"the user selects a business party type" should {
      "not store the partyType in backend and begin a business party type selection" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> PartyType.stati(BusinessEntity))))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.partners.routes.BusinessPartnerEntityTypeController.showPartnerType(partnerIndex).url)
      }
    }

    "redirect back to partner entity type page if submitted with index less than min partner count" in new Setup {
      val partnerIndex = 1
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type")
        .post(Map("value" -> PartyType.stati(BusinessEntity))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(PartnerIndexValidation.minPartnerIndex).url)
    }

    "redirect back to partner entity type page if submitted with index more than max partner count" in new Setup {
      val partnerIndex = 100
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type")
        .post(Map("value" -> PartyType.stati(BusinessEntity))))

      response.status mustBe SEE_OTHER
      response.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(appConfig.maxPartnerCount).url)
    }

    "the user submits incomplete form with missing party type" should {
      "throw an exception" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection[Entity](Some(List(Entity(None, Trust, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> "")))

        res.status mustBe BAD_REQUEST
      }
    }

    "the user submits an invalid lead partner" should {
      "throw an exception" in new Setup {
        val partnerIndex = 2

        given()
          .user.isAuthorised()
          .registrationApi.getListSection(Some(List(Entity(None, Partnership, Some(true), None, None, None, None))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(s"/partner/$partnerIndex/partner-type").post(Map("value" -> "55")))
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
