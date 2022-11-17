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

package controllers

import common.enums.VatRegStatus
import config.FrontendAppConfig
import itutil.ControllerISpec
import models.api.{Attached, Attachments, EligibilitySubmissionData, VatSchemeHeader}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class JourneyControllerISpec extends ControllerISpec {

  lazy val controller: JourneyController = app.injector.instanceOf(classOf[JourneyController])
  lazy val appConfig: FrontendAppConfig = app.injector.instanceOf(classOf[FrontendAppConfig])

  val showUrl: String = routes.JourneyController.show.url
  val submitUrl: String = routes.JourneyController.submit.url
  val newJourneyUrl: String = routes.JourneyController.startNewJourney.url

  def continueJourneyUrl(regId: String): String =
    routes.JourneyController.continueJourney(Some(regId)).url

  def initJourneyUrl(regId: String): String =
    routes.JourneyController.initJourney(regId).url

  val vatSchemeHeader: VatSchemeHeader = VatSchemeHeader(
    registrationId = "1",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    requiresAttachments = false
  )
  val vatSchemeHeader2: VatSchemeHeader = VatSchemeHeader(
    registrationId = "2",
    createdDate = testCreatedDate,
    status = VatRegStatus.draft,
    requiresAttachments = false
  )

  s"GET $showUrl" when {
    "the registrations API returns in flight registrations" must {
      "redirect to manage registrations page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getAllRegistrations(List(vatSchemeHeader, vatSchemeHeader2))

        val res: WSResponse = await(buildClient(showUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ManageRegistrationsController.show.url)
      }
    }

    "the registrations API returns no registrations" must {
      s"redirect to new journey page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getAllRegistrations(Nil)

        val res: WSResponse = await(buildClient(showUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
      }
    }
  }

  s"POST $submitUrl" when {
    "the user wants to start a new registration" must {
      "redirect to the new journey url" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Map("value" -> "true")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.startNewJourney.url)
      }
    }
    "the user wants to continue their existing registration" must {
      "redirect to the continue journey url" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Map("value" -> "false")))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.continueJourney(Some(testRegId)).url)
      }
    }
    "the user has not selected journey option" must {
      "return a BAD_REQUEST" in new Setup {
        given()
          .user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(""))
        res.status mustBe BAD_REQUEST
      }
    }
  }

  s"GET $newJourneyUrl" when {
    "redirect to the Application Reference page" when {
      "user is authenticated and authorised to access the app without profile" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.registrationCreated()
          .registrationApi.getSection(Some(VatRegStatus.draft))

        val res: WSResponse = await(buildClient(newJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationReferenceController.show.url)
      }
    }
  }

  s"GET ${continueJourneyUrl(testRegId)}" when {
    "the registration is submitted" must {
      "redirect to the application submission page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.submitted))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme.copy(status = VatRegStatus.submitted)))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationSubmissionController.show.url)
      }
    }
    "the registration requires attachments" must {
      "redirect to the documents required page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme).as[JsObject]
          ++ Json.obj("attachments" -> Json.toJson(Attachments(Some(Attached)))))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.attachments.routes.DocumentsRequiredController.resolve.url)
      }
    }
    "the eligibility section of the registration is populated and attachments aren't required" must {
      "redirect to the Task List" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData),
            applicationReference = Some("application reference")
          )).as[JsObject])
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
    "on failure with missing vat schema details for given reg-id" must {
      "return INTERNAL_SERVER_ERROR" in new Setup {
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "for missing journey id" must {
      "return BAD_REQUEST" in new Setup {
        given().user.isAuthorised()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(routes.JourneyController.continueJourney(None).url).get())
        res.status mustBe BAD_REQUEST
      }
    }
    "the application reference doesn't exist" must {
      "redirect to the Application Reference page" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(VatRegStatus.draft))
          .registrationApi.getRegistration(Json.toJson(emptyUkCompanyVatScheme))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ApplicationReferenceController.show.url)
      }
    }
  }

  s"GET ${routes.JourneyController.initJourney(testRegId)}" when {
    "eligibility data exists for the user" when {
      "the current profile has been set up successfully" when {
        "redirect to the Task List when the Task List" in new Setup {
          given()
            .user.isAuthorised(arn = Some(testArn))
            .registrationApi.getSection(Some(VatRegStatus.draft))
            .registrationApi.getSection(Some(testEligibilitySubmissionData.copy(isTransactor = true)))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

          res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
        }
      }
    }
    "eligibility data doesn't exist for the user" must {
      "redirect to the start of the journey" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[VatRegStatus.Value](None)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

        res.header(HeaderNames.LOCATION) mustBe Some(routes.JourneyController.show.url)
      }
    }
  }

}
