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

package controllers.otherbusinessinvolvements

import forms.otherbusinessinvolvements.CaptureUtrForm
import itutil.ControllerISpec
import models.OtherBusinessInvolvement
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureUtrControllerISpec extends ControllerISpec {
  val idx1: Int = 1
  val idx2: Int = 2

  lazy val testUtr = "1234567890"
  override lazy val fullOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(true),
    utr = Some(testUtr),
    stillTrading = Some(true)
  )

  def pageUrl(index: Int): String = routes.CaptureUtrController.show(index).url

  s"GET ${pageUrl(idx1)}" must {
    "return OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return OK with prepop" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById(CaptureUtrForm.captureUtrKey).attr("value") mustBe testUtr
      }
    }

    "return OK when data is incomplete" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement.copy(utr = None)), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) {
        _.status mustBe OK
      }
    }

    "redirect to minIdx page if given index is less than configured minIdx" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(0)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(pageUrl(idx1))
      }
    }
  }

  s"GET ${pageUrl(idx2)}" must {
    "return OK if it is a valid index" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](Some(List(fullOtherBusinessInvolvement)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return a redirect to a valid index" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx2))
        .registrationApi.getListSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(pageUrl(idx1))
      }
    }
  }

  s"POST ${pageUrl(idx1)}" must {
    "return a redirect to next page after storing in BE" in new Setup {
      val testNewUtr = "0987654321"
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement), idx = Some(idx1))
        .registrationApi.replaceSection(fullOtherBusinessInvolvement.copy(utr = Some(testNewUtr)), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).post(Map(CaptureUtrForm.captureUtrKey -> Seq(testNewUtr)))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessActivelyTradingController.show(idx1).url)
      }
    }

    "return a BAD_REQUEST if invalid data is submitted" in new Setup {
      val invalidUtr = "invalidUtr"

      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).post(Map(CaptureUtrForm.captureUtrKey -> Seq(invalidUtr)))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}