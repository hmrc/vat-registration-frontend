/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.registration.applicant

import common.enums.VatRegStatus
import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{StubIncorpIdJourney, UseSoleTraderIdentification}
import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{CharitableOrg, RegSociety, UkCompany, VatScheme}
import models.external.IncorporatedEntity
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{CREATED, await, _}

class IncorpIdControllerISpec extends ControllerISpec {

  val incorpDetailsJson: JsValue = Json.toJson(testIncorpDetails)(IncorporatedEntity.apiFormat)

  "GET /start-incorp-id-journey" should {
    "redirect to the returned journey url for UkCompany" in new Setup {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.contains(VatScheme(
        currentProfile.registrationId,
        status = VatRegStatus.draft,
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = UkCompany))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for RegSociety" in new Setup {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.contains(VatScheme(
        currentProfile.registrationId,
        status = VatRegStatus.draft,
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = RegSociety))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/registered-society-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }

    "redirect to the returned journey url for CharitableOrg" in new Setup {
      implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      disable(StubIncorpIdJourney)

      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .vatScheme.contains(VatScheme(
        currentProfile.registrationId,
        status = VatRegStatus.draft,
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = CharitableOrg))
      ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val testJourneyStartUrl = "/test"
      val testDeskProServiceId = "vrs"

      stubPost("/incorporated-entity-identification/api/charitable-organisation-journey", CREATED, Json.obj("journeyStartUrl" -> testJourneyStartUrl, "deskProServiceId" -> testDeskProServiceId).toString)

      val res: WSResponse = await(buildClient(applicantRoutes.IncorpIdController.startJourney().url).get)

      res.status mustBe SEE_OTHER
      res.header(LOCATION) mustBe Some(testJourneyStartUrl)
    }
  }

  "GET /incorp-id-callback" when {
    "when the UseSoleTraderIdentification feature switch is enabled" should {
      "redirect to STI" in {
        enable(UseSoleTraderIdentification)
        disable(StubIncorpIdJourney)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .vatScheme.contains(emptyUkCompanyVatScheme)

        stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

        val res = buildClient("/incorp-id-callback?journeyId=1").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.SoleTraderIdentificationController.startJourney().url)
        }
      }
    }

    "when the UseSoleTraderIdentification feature switch is disabled" should {
      "redirect to PDV" in {
        disable(UseSoleTraderIdentification)
        disable(StubIncorpIdJourney)

        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .s4lContainer[ApplicantDetails].contains(ApplicantDetails())
          .s4lContainer[ApplicantDetails].isUpdatedWith(ApplicantDetails(entity = Some(testIncorpDetails)))
          .vatScheme.has("applicant-details", Json.toJson(ApplicantDetails()))
          .vatScheme.contains(emptyUkCompanyVatScheme)

        stubGet("/incorporated-entity-identification/api/journey/1", OK, incorpDetailsJson.toString)

        val res = buildClient("/incorp-id-callback?journeyId=1").get()

        whenReady(res) { result =>
          result.status mustBe SEE_OTHER
          result.headers(LOCATION) must contain(applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url)
        }
      }
    }
  }

}
