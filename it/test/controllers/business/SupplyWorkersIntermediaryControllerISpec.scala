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

package controllers.business

import itutil.ControllerISpec
import models.api._
import models.{Business, LabourCompliance}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class SupplyWorkersIntermediaryControllerISpec extends ControllerISpec {

  "intermediary workers controller" should {
    "return OK on show and users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/arrange-supply-of-workers").get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return SEE_OTHER on submit redirecting to Import of Export for UkCompany" in new Setup {
      val initialModel: Business = fullModel.copy(labourCompliance = None)
      val expectedModel: Business = initialModel.copy(labourCompliance = Some(LabourCompliance(None, Some(true), None)))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))


      insertCurrentProfileIntoDb(currentProfile, sessionString)

      verifyRedirectForGivenPartyType(controllers.routes.TaskListController.show.url)
    }

    "return SEE_OTHER on submit redirecting to Turnover for NonUkCompany" in new Setup {
      val initialModel: Business = fullModel.copy(labourCompliance = None)
      val expectedModel: Business = initialModel.copy(labourCompliance = Some(LabourCompliance(None, Some(true), None)))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      verifyRedirectForGivenPartyType(controllers.routes.TaskListController.show.url)
    }
  }

  private def verifyRedirectForGivenPartyType(redirectUrl: String) = {
    val response = buildClient("/arrange-supply-of-workers").post(Map("value" -> Seq("true")))

    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(redirectUrl)
    }
  }
}
