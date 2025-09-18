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

package controllers.sicandcompliance

import helpers.RequestsFinder
import itutil.ControllerISpec
import models.Business
import models.api.EligibilitySubmissionData
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class MainBusinessActivityControllerISpec extends ControllerISpec with RequestsFinder {

  "MainBusinessActivity on show returns OK" in new Setup {
    given()
      .user.isAuthorised()

    insertIntoDb(sessionString, sicCodeMapping)

    val response: Future[WSResponse] = buildClient(routes.MainBusinessActivityController.show.url).get()
    whenReady(response) { res =>
      res.status mustBe OK
    }
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete" in new Setup {
    val incompleteModelWithoutSicCode: Business = fullModel.copy(mainBusinessActivity = None)
    val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
    given()
      .user.isAuthorised()
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
      .registrationApi.getSection[Business](Some(expectedUpdateToBusiness))

    insertIntoDb(sessionString, sicCodeMapping)

    val response: Future[WSResponse] = buildClient(routes.MainBusinessActivityController.submit.url).post(Map("value" -> Seq(sicCodeId)))
    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve.url)
    }
  }

}
