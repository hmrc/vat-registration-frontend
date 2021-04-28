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
import models.api.trafficmanagement.{OTRS, VatReg}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import services.SaveAndRetrieveService.vatSchemeKey

import java.time.LocalDate

class WelcomeControllerISpec extends ControllerISpec {

  lazy val controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])
  lazy val appConfig: FrontendAppConfig = app.injector.instanceOf(classOf[FrontendAppConfig])

  val thresholdUrl = s"/vatreg/threshold/${LocalDate.now()}"
  val currentThreshold = "50000"

  val startUrl: String = routes.WelcomeController.show().url
  val newJourneyUrl: String = routes.WelcomeController.startNewJourney().url
  val continueJourneyUrl: String = routes.WelcomeController.continueJourney().url

  val startNewApplicationPageUrl: String = routes.StartNewApplicationController.show().url

  val vatSchemeJson: String = Json.toJson(fullVatScheme).toString()

  s"GET $startUrl" when {
    "SaveAndContinueLater FS is disabled" must {
      s"return a redirect to $newJourneyUrl" when {
        "user is authenticated and authorised to access the app without profile" in new Setup {
          disable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .vatRegistrationFootprint.exists()
            .vatScheme.regStatus(VatRegStatus.draft.toString)
            .audit.writesAudit()
            .audit.writesAuditMerged()

          val res: WSResponse = await(buildClient(startUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }

        "user is authenticated and authorised to access the app with profile" in new Setup {
          disable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .audit.writesAudit()
            .audit.writesAuditMerged()

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res: WSResponse = await(buildClient(startUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }
    }

    "SaveAndContinueLater FS is enabled" must {
      s"return a redirect to $newJourneyUrl" when {
        "the traffic management check fails" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .audit.writesAudit()
            .audit.writesAuditMerged()
            .trafficManagement.fails

          val res: WSResponse = await(buildClient(startUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
        }
      }

      s"return a redirect to $startNewApplicationPageUrl" when {
        "the traffic management check passes" in new Setup {
          enable(SaveAndContinueLater)

          given()
            .user.isAuthorised
            .vatRegistrationFootprint.exists()
            .vatScheme.regStatus(VatRegStatus.draft.toString)
            .audit.writesAudit()
            .audit.writesAuditMerged()
            .trafficManagement.passes()

          val res: WSResponse = await(buildClient(startUrl).get())

          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(startNewApplicationPageUrl)
        }
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
          .audit.writesAudit()
          .audit.writesAuditMerged()

        val res: WSResponse = await(buildClient(newJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityUrl)
      }

      "user is authenticated and authorised to access the app with profile" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res: WSResponse = await(buildClient(newJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityUrl)
      }
    }
  }

  s"GET $continueJourneyUrl" must {
    "return a redirect to eligiblity" when {
      "user has a vatreg application in progress with a partial vat scheme" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.regStatus(VatRegStatus.draft.toString)
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .trafficManagement.passes(VatReg)
          .s4l.contains(vatSchemeKey, vatSchemeJson)
          .vatRegistration.insertScheme(vatSchemeJson)

        val res: WSResponse = await(buildClient(continueJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityRouteUrl)
      }

      "user has a vatreg application in progress without a partial vat scheme" in new Setup {
        given()
          .user.isAuthorised
          .vatScheme.regStatus(VatRegStatus.draft.toString)
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .trafficManagement.passes(VatReg)
          .s4l.isEmpty()
          .vatRegistration.insertScheme(vatSchemeJson)

        val res: WSResponse = await(buildClient(continueJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.eligibilityRouteUrl)
      }
    }

    "return a redirect to otrs" when {
      "user has an otrs application in progress" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .trafficManagement.passes(OTRS)

        val res: WSResponse = await(buildClient(continueJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(appConfig.otrsRoute)
      }
    }

    s"return a redirect to $newJourneyUrl" when {
      "user doesn't pass traffic management" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .trafficManagement.fails

        val res: WSResponse = await(buildClient(continueJourneyUrl).get())

        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(newJourneyUrl)
      }
    }
  }
}
