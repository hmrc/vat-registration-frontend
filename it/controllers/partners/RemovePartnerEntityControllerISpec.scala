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

package controllers.partners

import controllers.partners.PartnerIndexValidation.minPartnerIndex
import itutil.ControllerISpec
import models.Entity
import models.api._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class RemovePartnerEntityControllerISpec extends ControllerISpec {
  def pageGetUrl(index: Int): String = routes.RemovePartnerEntityController.show(index).url
  def pagePostUrl(index: Int): String = routes.RemovePartnerEntityController.submit(Some("testPartnerName"), index).url

  s"GET ${pageGetUrl(minPartnerIndex - 1)}" must {
    "redirect to minIdx page if given index is less than minIdx" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None, None, None, None)), idx = Some(minPartnerIndex))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageGetUrl(minPartnerIndex - 1)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(pageGetUrl(minPartnerIndex))
      }
    }

    "redirect to partner entity page when requested for invalid index and only lead partner available" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None, None, None, None)), idx = Some(minPartnerIndex))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageGetUrl(minPartnerIndex)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

  s"GET ${pageGetUrl(minPartnerIndex)}" must {
    "return OK if it is a valid index" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
          Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageGetUrl(minPartnerIndex)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  s"POST ${pagePostUrl(minPartnerIndex)}" must {
    "return a redirect to summary page after deleting the partner entity type with correct index" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.deleteSection[Entity](optIdx = Some(minPartnerIndex))
        .registrationApi.getListSection[Entity](Some(List(
          Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
          Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
        )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pagePostUrl(minPartnerIndex)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerSummaryController.show.url)
      }
    }

    "return a redirect to partner summary page after deleting and no of partners are now under the max limit" in new Setup {
      val testAttachmentDetails: Attachments = Attachments(Some(Attached), None, None, Some(true))
      val entities: List[Entity] = List.fill(10)(Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None))

      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(entities))
        .registrationApi.getSection[Attachments](Some(testAttachmentDetails))
        .registrationApi.deleteSection[Entity](optIdx = Some(minPartnerIndex))
        .registrationApi.replaceSection[Attachments](testAttachmentDetails.copy(additionalPartnersDocuments = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pagePostUrl(minPartnerIndex)).post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerSummaryController.show.url)
      }
    }

    "return a redirect to summary page when user chooses 'No'" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(
          List(
            Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
            Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
          )
        ))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pagePostUrl(minPartnerIndex)).post(Map("value" -> Seq("false")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerSummaryController.show.url)
      }
    }

    "return BAD_REQUEST if none of the option is selected" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pagePostUrl(minPartnerIndex)).post(Map("value" -> Seq("")))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}