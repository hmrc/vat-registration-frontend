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

import featuretoggle.FeatureToggleSupport
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NonUkNonEstablished}
import models.{Business, LabourCompliance}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.test.Helpers._
import views.html.sicandcompliance.Workers

class WorkersControllerISpec extends ControllerISpec with FeatureToggleSupport {

  val view = app.injector.instanceOf[Workers]

  "show" should {
    "return OK with the form unpopulated" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/number-of-workers-supplied").get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("#numberOfWorkers").attr("value") mustBe ""
      }
    }
    "return OK with the form prepopulated" in new Setup {
      val dataModel = fullModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Business](Some(dataModel))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/number-of-workers-supplied").get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).select("#numberOfWorkers").attr("value") mustBe "1"
      }
    }
  }

  "submit" should {
    "redirect to the Task List" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.replaceSection[Business](expectedModel)
        .registrationApi.getSection[Business](Some(expectedModel))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> Seq("1")))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "return BAD_REQUEST" in new Setup {
      val initialModel = fullModel.copy(labourCompliance = Some(LabourCompliance(None, None, Some(true))))
      val expectedModel = initialModel.copy(labourCompliance = Some(LabourCompliance(Some(1), None, Some(true))))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))
        .registrationApi.replaceSection[Business](expectedModel)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient("/number-of-workers-supplied").post(Map("numberOfWorkers" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }

}
