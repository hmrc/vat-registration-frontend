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
import featureswitch.core.config.SaveAndContinueLater
import itutil.ControllerISpec
import models.api.EligibilitySubmissionData
import models.api.trafficmanagement.{OTRS, VatReg}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import support.RegistrationsApiStubs

import java.time.LocalDate

class WelcomeControllerISpec extends ControllerISpec
  with RegistrationsApiStubs {

  lazy val controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])
  lazy val appConfig: FrontendAppConfig = app.injector.instanceOf(classOf[FrontendAppConfig])

  val thresholdUrl = s"/vatreg/threshold/${LocalDate.now()}"
  val currentThreshold = "50000"

  val showUrl: String = routes.WelcomeController.show.url
  val submitUrl: String = routes.WelcomeController.submit.url
  val newJourneyUrl: String = routes.WelcomeController.startNewJourney.url

  def continueJourneyUrl(regId: String): String =
    routes.WelcomeController.continueJourney(Some(regId)).url

  def initJourneyUrl(regId: String): String =
    routes.WelcomeController.initJourney(regId).url

  val vatSchemeJson = Json.toJson(fullVatScheme)
  val vatSchemeJson2 = Json.toJson(fullVatScheme.copy(id = "2"))

  s"GET $showUrl" when {
    "SaveAndContinueLater FS is disabled" must {
      s"return a redirect to $newJourneyUrl" when {
        "user is authenticated and authorised to access the app without profile" in new Setup {
          disable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .vatRegistrationFootprint.exists()
            .vatScheme.regStatus(VatRegStatus.draft.toString)

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }

        "user is authenticated and authorised to access the app with profile" in new Setup {
          disable(SaveAndContinueLater)

          given()
            .user.isAuthorised

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }
    }

    "SaveAndContinueLater FS is enabled" when {
      "the registrations API returns no registrations" must {
        s"redirect to $newJourneyUrl" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised

          registrationsApi.GET.respondsWith(OK, Some(Json.arr()))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }

      "the registratsions API returns 1 in flight registration" must {
        "return OK and display the select registration page" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .audit.writesAudit()
            .audit.writesAuditMerged()
            .vatScheme.regStatus(VatRegStatus.draft.toString)

          registrationsApi.GET.respondsWith(OK, Some(Json.arr(vatSchemeJson)))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe OK
        }
      }

      "the registrations API returns more than 1 in flight registration" must {
        "return OK and display the select registration page" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .vatScheme.regStatus(VatRegStatus.draft.toString)

          registrationsApi.GET.respondsWith(OK, Some(Json.arr(vatSchemeJson2, vatSchemeJson)))

          val res: WSResponse = await(buildClient(showUrl).get())

          res.status mustBe OK
        }
      }
    }
  }

  s"POST $submitUrl" when {
    "the user wants to start a new registration" must {
      "redirect to the new journey url" in new Setup {
        given()
          .user.isAuthorised
          .trafficManagement.isCleared
          .vatScheme.deleted

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Json.obj("value" -> true)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.WelcomeController.startNewJourney.url)
      }
    }
    "the user wants to continue their existing registration" must {
      "redirect to the continue journey url" in new Setup {
        given()
          .user.isAuthorised

        insertCurrentProfileIntoDb(currentProfile, sessionId)
        val res: WSResponse = await(buildClient(showUrl).post(Json.obj("value" -> false)))

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.WelcomeController.continueJourney(Some(testRegId)).url)
      }
    }
  }

  s"GET $newJourneyUrl" must {
    "return a redirect to start of eligibility" when {
      "user is authenticated and authorised to access the app without profile" in new Setup {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists()
          .vatScheme.regStatus(VatRegStatus.draft.toString)

        val res: WSResponse = await(buildClient(newJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityStartUrl(testRegId))
      }
    }
  }

  s"GET ${continueJourneyUrl(testRegId)}" when {
    "the channel for traffic management is VatReg" must {
      "redirect to Eligibiilty" in new Setup {
        given()
          .user.isAuthorised
          .s4l.contains("partialVatScheme", Json.stringify(vatSchemeJson))
          .vatRegistration.insertScheme(Json.stringify(vatSchemeJson))
          .vatScheme.regStatus(VatRegStatus.draft.toString)
          .trafficManagement.passes(VatReg)
          .vatRegistrationFootprint.exists()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityStartUrl(testRegId))
      }
    }
    "the channel for traffic management is OTRS" must {
      "redirect to OTRS" in new Setup {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists()
          .trafficManagement.passes(OTRS)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.otrsRoute)
      }
    }
    "traffic management fails" must {
      "start a new journey" in new Setup {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists()
          .trafficManagement.fails

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(continueJourneyUrl(testRegId)).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
      }
    }
  }

  s"GET ${routes.WelcomeController.initJourney(testRegId)}" when {
    "eligibility data exists for the user" when {
      "the current profile has been set up successfully" must {
        "redirect to the Honesty Declaration page" in new Setup {
          given()
            .user.isAuthorised
            .trafficManagement.passes()
            .vatScheme.regStatus(VatRegStatus.draft.toString)
            .s4l.isUpdatedWith("CurrentProfile", Json.stringify(Json.toJson(currentProfile)))

          sectionsApi(testRegId, EligibilitySubmissionData.apiKey.key)
            .GET.respondsWith(OK, Some(Json.obj("data" -> Json.toJson(testEligibilitySubmissionData))))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

          res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
        }
      }
    }
    "eligibility data doesn't exist for the user" must {
      "redirect to the start of the journey" in new Setup {
        given()
          .user.isAuthorised
          .trafficManagement.passes()

        sectionsApi(testRegId, EligibilitySubmissionData.apiKey.key)
          .GET.respondsWith(NOT_FOUND, None)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(initJourneyUrl(testRegId)).get())

        res.header(HeaderNames.LOCATION) mustBe Some(routes.WelcomeController.show.url)
      }
    }
  }

}
