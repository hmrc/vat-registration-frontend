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

import forms.otherbusinessinvolvements.CaptureVrnForm
import itutil.ControllerISpec
import models.OtherBusinessInvolvement
import models.api.EligibilitySubmissionData
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class CaptureVrnControllerISpec extends ControllerISpec {
  val idx1: Int = 1
  val idx2: Int = 2

  def pageUrl(index: Int): String = routes.CaptureVrnController.show(index).url

  s"GET ${pageUrl(idx1)}" must {
    "return OK" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById(CaptureVrnForm.captureVrnKey).attr("value") mustBe testVrn
      }
    }

    "return OK when data incomplete" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement.copy(vrn = None)), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).get()

      whenReady(response) {
        _.status mustBe OK
      }
    }

    "redirect to minIdx if given index is less than minIdx" in new Setup {
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](None, idx = Some(idx1))
        .registrationApi.getListSection[OtherBusinessInvolvement](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

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

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageUrl(idx2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(pageUrl(idx1))
      }
    }
  }

  s"POST ${pageUrl(idx1)}" must {
    "return a redirect to next page after storing in BE" in new Setup {
      val testNewVrn = "987654353"
      given()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[OtherBusinessInvolvement](Some(fullOtherBusinessInvolvement), idx = Some(idx1))
        .registrationApi.replaceSection(fullOtherBusinessInvolvement.copy(vrn = Some(testNewVrn)), idx = Some(idx1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).post(Map(CaptureVrnForm.captureVrnKey -> Seq(testNewVrn)))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.OtherBusinessActivelyTradingController.show(idx1).url)
      }
    }

    "return BAD_REQUEST if form has errors" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(pageUrl(idx1)).post("")
      whenReady(response) {
        _.status mustBe BAD_REQUEST
      }
    }
  }
}