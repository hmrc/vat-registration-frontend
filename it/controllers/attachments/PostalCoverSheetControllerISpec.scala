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
import featureswitch.core.config.FeatureSwitching
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import models.{ApiKey, ApplicantDetails, CurrentProfile, TransactorDetails}
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class PostalCoverSheetControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.attachments.routes.PostalCoverSheetController.show.url

  val testAckRef = "VRN1234567"

  val profile: CurrentProfile = currentProfile.copy(vatRegistrationStatus = VatRegStatus.submitted)

  s"GET $url" must {
    "return an OK" in new Setup {
      implicit val key: ApiKey[String] = acknowledgementReferenceKey
      given()
        .user.isAuthorised()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection(Some(testAckRef))

      insertCurrentProfileIntoDb(profile, sessionId)

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

      insertCurrentProfileIntoDb(profile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }
}
