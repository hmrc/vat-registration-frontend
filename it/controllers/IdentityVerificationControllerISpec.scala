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

import fixtures.LodgingOfficerFixture
import helpers.RequestsFinder
import models.IVResult
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import repositories.ReactiveMongoRepository
import support.AppAndStubs
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.VATRegFeatureSwitch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class IdentityVerificationControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder with LodgingOfficerFixture {

  val featureSwitch: VATRegFeatureSwitch = app.injector.instanceOf[VATRegFeatureSwitch]

  val blockKey = "officer-data"

  val officerJson = Json.parse(
    s"""
       |{
       |  "name": {
       |    "first": "${validOfficer.name.forename}",
       |    "middle": "${validOfficer.name.otherForenames}",
       |    "last": "${validOfficer.name.surname}"
       |  },
       |  "role": "${validOfficer.role}",
       |  "dob": "$officerDob",
       |  "nino": "$officerNino"
       |}""".stripMargin)

  class Setup {
    import scala.concurrent.duration._

    def customAwait[A](future: Future[A])(implicit timeout: Duration): A = Await.result(future, timeout)
    val repo = new ReactiveMongoRepository(app.configuration, mongo)
    val defaultTimeout: FiniteDuration = 5 seconds

    customAwait(repo.ensureIndexes)(defaultTimeout)
    customAwait(repo.drop)(defaultTimeout)

    def insertCurrentProfileIntoDb(currentProfile: models.CurrentProfile, sessionId : String): Boolean = {
      val preawait = customAwait(repo.count)(defaultTimeout)
      val currentProfileMapping: Map[String, JsValue] = Map("CurrentProfile" -> Json.toJson(currentProfile))
      val res = customAwait(repo.upsert(CacheMap(sessionId, currentProfileMapping)))(defaultTimeout)
      customAwait(repo.count)(defaultTimeout) mustBe preawait + 1
      res
    }
  }

  "GET Complete IV Journey" should {
    "redirect to 'Have you ever changed your name?' page" in new Setup() {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.Success)
        .setIvStatus.setStatus()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivComplete").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.OfficerController.showFormerName().url)
      }
    }

    "return 303 if VAT Backend does not return a 200 status when saving ivPassed" in new Setup() {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.Success)
        .setIvStatus.setStatus(status = 404)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivComplete").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.callbacks.routes.SignInOutController.errorShow().url)
      }
    }
  }
  "GET Redirect IV" should {
    "redirect to the link returned from IV Proxy" in new Setup() {
      featureSwitch.manager.disable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .s4l.isUpdatedWith("IVJourneyID", JsString("12345").toString)
        .vatScheme.has(blockKey, officerJson)
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .iv.startJourney(200)

      insertCurrentProfileIntoDb(currentProfile.copy(ivPassed = Some(false)), sessionId)

      val response = buildClient(s"/start-iv-journey").get()
      whenReady(response){res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION).get.contains("""/foo/bar/and/wizz""") mustBe true
      }
    }
    "redirect to the link returned from IV stub" in new Setup() {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .vatScheme.has(blockKey, officerJson)
        .s4l.isUpdatedWith("IVJourneyID", JsString("12345").toString)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile.copy(ivPassed = Some(false)), sessionId)

      val response = buildClient(s"/start-iv-journey").get()
      whenReady(response){res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION).get.contains("""test-iv-response""") mustBe true
      }
    }
  }

  "GET Failed IV Journey" should {
    "redirect to correct Timeout error page" in new Setup() {
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      val journeyId = "12345"
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.Timeout)
        .setIvStatus.setStatus(ivPassed = false)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IdentityVerificationController.timeoutIV().url)
      }
    }

    "redirect to correct Unable to confirm identity error page" in new Setup() {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .iv.outcome(journeyId, IVResult.InsufficientEvidence)
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .setIvStatus.setStatus(ivPassed = false)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IdentityVerificationController.unableToConfirmIdentity().url)
      }
    }

    "redirect to correct Failed IV error page" in new Setup() {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.FailedIV)
        .setIvStatus.setStatus(ivPassed = false)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IdentityVerificationController.failedIV().url)
      }
    }

    "redirect to correct Locked out error page" in new Setup() {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.LockedOut)
        .setIvStatus.setStatus(ivPassed = false)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.IdentityVerificationController.lockedOut().url)
      }
    }

    "redirect to correct User aborted error page" in new Setup() {
      val journeyId = "12345"
      featureSwitch.manager.enable(featureSwitch.useIvStub)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .s4l.contains("IVJourneyID", JsString(journeyId).toString)
        .iv.outcome(journeyId, IVResult.UserAborted)
        .setIvStatus.setStatus(ivPassed = false)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some("/register-for-vat/incomplete-identity-check")
      }
    }

  }
}
