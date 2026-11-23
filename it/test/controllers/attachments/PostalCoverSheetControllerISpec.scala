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

package controllers.attachments

import common.enums.VatRegStatus
import connectors.RegistrationApiConnector.acknowledgementReferenceKey
import featuretoggle.FeatureSwitch.VrsNewAttachmentJourney
import featuretoggle.FeatureToggleSupport
import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import models.{ApiKey, ApplicantDetails, CurrentProfile, TransactorDetails}
import play.api.libs.json.{Format, JsBoolean, JsValue, Json}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class PostalCoverSheetControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureToggleSupport {

  val url: String = controllers.attachments.routes.PostalCoverSheetController.show.url

  val testAckRef = "VRN1234567"

  val profile: CurrentProfile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)

  val coverLetterConfirmedSession: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(profile),
    "coverLetterConfirmed" -> JsBoolean(true)
  )

  override def afterEach(): Unit = {
    super.afterEach()
    disable(VrsNewAttachmentJourney)
  }

  s"GET $url" must {
    "return an OK" in new Setup {
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "return an OK for a transactor" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "show the printed checkbox when VrsNewAttachmentJourney is enabled" in new Setup {
      enable(VrsNewAttachmentJourney)
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
        Jsoup.parse(res.body).select("input[type=checkbox][name=value]").size mustBe 1
      }
    }

    "not show the printed checkbox when VrsNewAttachmentJourney is disabled" in new Setup {
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
        Jsoup.parse(res.body).select("input[type=checkbox][name=value]").size mustBe 0
      }
    }
    "redirect to the application submitted page when the cover letter has already been confirmed" in new Setup {
      enable(VrsNewAttachmentJourney)
      given().user.isAuthorised()

      insertIntoDb(sessionString, coverLetterConfirmedSession)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "ignore the confirmed flag when VrsNewAttachmentJourney is disabled" in new Setup {
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertIntoDb(sessionString, coverLetterConfirmedSession)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }

  s"POST $url" must {
    "redirect to the application submitted page when the checkbox is ticked" in new Setup {
      enable(VrsNewAttachmentJourney)
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
      }

      whenReady(buildClient(url).get()) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "redirect to the application submitted page when the cover letter has already been confirmed" in new Setup {
      enable(VrsNewAttachmentJourney)
      given().user.isAuthorised()

      insertIntoDb(sessionString, coverLetterConfirmedSession)

      val response: Future[WSResponse] = buildClient(url).post(Map[String, Seq[String]]())
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.ApplicationSubmissionController.show.url)
      }
    }

    "return a BAD_REQUEST with an error when the checkbox is not ticked" in new Setup {
      enable(VrsNewAttachmentJourney)
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map[String, Seq[String]]())
      whenReady(response) { res =>
        res.status mustBe 400
        Jsoup.parse(res.body).select(".govuk-error-summary").text must include("Select if you have printed or saved your cover letter")
      }
    }

    "redirect back to the page when VrsNewAttachmentJourney is disabled" in new Setup {
      given().user.isAuthorised()

      insertCurrentProfileIntoDb(profile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(url)
      }
    }
  }
}
