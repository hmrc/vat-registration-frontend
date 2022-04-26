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

package controllers.fileupload

import itutil.ControllerISpec
import models.api.{AttachmentType, EligibilitySubmissionData, PrimaryIdentityEvidence}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class UploadDocumentControllerISpec extends ControllerISpec {

  val url: String = controllers.fileupload.routes.UploadDocumentController.show.url

  val testReference = "testReference"

  s"GET $url" must {
    "return an OK when there's an incomplete attachment" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getIncompleteAttachments(List(PrimaryIdentityEvidence))
        .upscanApi.upscanInitiate(testReference)
        .upscanApi.storeUpscanReference(testReference, PrimaryIdentityEvidence)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "redirect to CYA when all attachments are complete" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .attachmentsApi.getIncompleteAttachments(List[AttachmentType]())

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.SummaryController.show.url)
      }
    }
  }

}
