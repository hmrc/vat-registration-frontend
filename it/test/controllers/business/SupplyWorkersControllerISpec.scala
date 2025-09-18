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
import models._
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class SupplyWorkersControllerISpec extends ControllerISpec {

  "supply workers controllers" should {
    "return OK on Show AND users answer is pre-popped on page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/supply-of-workers").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return INTERNAL_SERVER_ERROR if not authorised on show" in new Setup {
      given()
        .user.isNotAuthorised

      val response: Future[WSResponse] = buildClient("/supply-of-workers").get()
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "redirect on submit to populate backend not vat as model is incomplete" in new Setup {
      val incompleteModel: Business = fullModel.copy(businessDescription = None)
      val toBeUpdatedModel: Business = incompleteModel.copy(labourCompliance = Some(labourCompliance.copy(intermediaryArrangement = None)))

      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](toBeUpdatedModel)
        .registrationApi.getSection[Business](Some(toBeUpdatedModel))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/supply-of-workers").post(Map("value" -> Seq("true")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.WorkersController.show.url)
      }
    }
  }

}
