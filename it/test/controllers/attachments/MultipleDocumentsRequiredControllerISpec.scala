/*
 * Copyright 2024 HM Revenue & Customs
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

import itutil.ControllerISpec
import models.api._
import models.{ApplicantDetails, TransactorDetails}
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class MultipleDocumentsRequiredControllerISpec extends ControllerISpec {

  val showUrl: String = routes.MultipleDocumentsRequiredController.show.url

  s"GET $showUrl" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }

    "return OK for a transactor" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getAttachments(List[AttachmentType](IdentityEvidence, VAT2))
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(showUrl).get()

      whenReady(res) { result =>
        result.status mustBe OK
      }
    }
  }

}
