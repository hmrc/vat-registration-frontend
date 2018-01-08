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

import javax.inject.Inject

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import common.enums.{IVResult, VatRegStatus}
import helpers.RequestsFinder
import it.fixtures.VatRegistrationFixture
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.Json
import support.AppAndStubs
import models.{S4LVatLodgingOfficer, CurrentProfile => cp}
import utils.VATRegFeatureSwitch

class IdentityVerificationControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with VatRegistrationFixture {

  val featureSwitch: VATRegFeatureSwitch = app.injector.instanceOf[VATRegFeatureSwitch]

  "GET Complete IV Journey" should {
    "redirect to 'Have you ever changed your name?' page" in {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .getS4LJourneyID.stubS4LGetIV()
        .iv.outcome(journeyId, IVResult.Success)
        .setIvStatus.setStatus()
        .currentProfile.putKeyStoreValue("CurrentProfile", Json.toJson(cp("foo", "bar", "fizz", VatRegStatus.draft, None, Some(true))).toString())
        val response = buildClient(s"/ivComplete").get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(features.officer.controllers.routes.FormerNameController.show().url)
        }
      }
    "return 500 if VAT Backend does not return a 200 status when saving ivPassed" in {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .getS4LJourneyID.stubS4LGetIV()
        .iv.outcome(journeyId, IVResult.Success)
        .setIvStatus.setStatus(status = 404)
      val response = buildClient(s"/ivComplete").get()
      whenReady(response) { res =>
        res.status mustBe 500
        res.header(HeaderNames.LOCATION) mustBe None
      }
    }
  }
  "GET Redirect IV" should {
    "redirect to the link returned from IV Proxy" in {
      featureSwitch.manager.disable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv, Some(STARTED), Some("Lodging Officer retrieved from S4L"))
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"), ivPassed =  false)
        .getS4LJourneyID.stubS4LPutIV(currentState = Some("Lodging Officer retrieved from S4L"), nextState = Some("IV updated in S4L"))
        .getS4LJourneyID.stubS4LGetIV(currentState = Some("IV updated in S4L"), nextState = Some("IV retrieved from S4L"))
        .audit.writesAudit()
        .iv.startJourney(200)

        val response = buildClient(s"/start-iv-journey").get()
        whenReady(response){res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get.contains("""/foo/bar/and/wizz""") mustBe true
        }
    }
    "redirect to the link returned from IV stub" in {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"), ivPassed =  false)
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv, Some(STARTED), Some("Lodging Officer retrieved from S4L"))
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .getS4LJourneyID.stubS4LPutIV(currentState = Some("Lodging Officer retrieved from S4L"), nextState = Some("IV updated in S4L"))
        .getS4LJourneyID.stubS4LGetIV(currentState = Some("IV updated in S4L"), nextState = Some("IV retrieved from S4L"))
        .audit.writesAudit()
        val response = buildClient(s"/start-iv-journey").get()
        whenReady(response){res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get.contains("""test-iv-response""") mustBe true
        }
    }
  }

  "GET Failed IV Journey" should {
    "redirect to correct Timeout error page" in {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .getS4LJourneyID.stubS4LGetIV()
        .iv.outcome(journeyId, IVResult.Timeout)
        .setIvStatus.setStatus(ivPassed = false)
        .currentProfile.putKeyStoreValue("CurrentProfile", Json.toJson(cp("foo", "bar", "fizz", VatRegStatus.draft, None, Some(false))).toString())
        val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.iv.routes.IdentityVerificationController.timeoutIV().url)
      }
    }

    "redirect to correct Unable to confirm identity error page" in {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .iv.outcome(journeyId, IVResult.InsufficientEvidence)
        .getS4LJourneyID.stubS4LGetIV()
        .setIvStatus.setStatus(ivPassed = false)
        .currentProfile.putKeyStoreValue("CurrentProfile", Json.toJson(cp("foo", "bar", "fizz", VatRegStatus.draft, None, Some(false))).toString())
        val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.iv.routes.IdentityVerificationController.unableToConfirmIdentity().url)
        }
    }

    "redirect to correct Failed IV error page" in {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .getS4LJourneyID.stubS4LGetIV()
        .iv.outcome(journeyId, IVResult.FailedIV)
        .setIvStatus.setStatus(ivPassed = false)
        .currentProfile.putKeyStoreValue("CurrentProfile", Json.toJson(cp("foo", "bar", "fizz", VatRegStatus.draft, None, Some(false))).toString())
      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.iv.routes.IdentityVerificationController.failedIV().url)
      }
    }

      "redirect to correct Locked out error page" in {
        val journeyId = "12345"
        featureSwitch.manager.enable(featureSwitch.useIvStub)
        given()
          .user.isAuthorised
          .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
          .audit.writesAudit()
          .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
          .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
          .getS4LJourneyID.stubS4LGetIV()
          .iv.outcome(journeyId, IVResult.LockedOut)
          .setIvStatus.setStatus(ivPassed = false)
          .currentProfile.putKeyStoreValue("CurrentProfile", Json.toJson(cp("foo", "bar", "fizz", VatRegStatus.draft, None, Some(false))).toString())
        val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.iv.routes.IdentityVerificationController.lockedOut().url)
        }
      }

    "redirect to correct User aborted error page" in {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
          .audit.writesAuditMerged()
        .s4lContainerInScenario[S4LVatLodgingOfficer].contains(validS4LLodgingOfficerPreIv)
        .vatScheme.isUpdatedWith(validLodgingOfficerPreIV)
        .getS4LJourneyID.stubS4LGetIV()
        .iv.outcome(journeyId, IVResult.UserAborted)
        .setIvStatus.setStatus(ivPassed = false)
        .currentProfile.putKeyStoreValue("CurrentProfile",Json.toJson(cp("foo","bar","fizz",VatRegStatus.draft,None, Some(false))).toString())
        val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/incomplete-identity-check")
        }
      }

}}
