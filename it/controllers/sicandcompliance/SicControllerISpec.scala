/*
 * Copyright 2017 HM Revenue & Customs
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

import controllers.business
import helpers.RequestsFinder
import itutil.ControllerISpec
import models.Business.s4lKey
import models._
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.test.Helpers._

class SicControllerISpec extends ControllerISpec with RequestsFinder {

  "SicHalt on show returns OK" in new Setup {
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)


    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient("/choose-standard-industry-classification-codes").get()
    whenReady(response) { res =>
      res.status mustBe OK
    }
  }

  "User submitted on the sic halt page should redirect them to ICL, prepopping sic codes from VR" in new Setup {
    val simplifiedSicJson =
      """|{"businessActivities" : [
         |           {
         |               "code" : "43220",
         |               "desc" : "Plumbing, heat and air-conditioning installation",
         |               "indexes" : ""
         |           }
         |       ]
         |}""".stripMargin

    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .icl.setup()

    insertIntoDb(sessionId, sicCodeMapping)

    val mockedPostToICL = buildClient("/choose-standard-industry-classification-codes").post(Map("" -> Seq()))

    whenReady(mockedPostToICL) { res =>
      res.status mustBe SEE_OTHER
    }
  }

  "User submitted on the sic halt page should redirect them to ICL, prepopping sic codes from II" in new Setup {
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .icl.setup()

    insertIntoDb(sessionId, sicCodeMapping)

    val mockedPostToICL = buildClient("/choose-standard-industry-classification-codes").post(Map("" -> Seq()))

    whenReady(mockedPostToICL) { res =>
      res.status mustBe SEE_OTHER
    }
  }

  "Returning from ICL with 1 SIC code (non compliance) should fetch sic codes, save in keystore and return a SEE_OTHER" in new Setup {
    val sicCode = SicCode("23456", "This is a fake description", "")

    val expectedUpdateToBusiness: Business = fullModel.copy(
      businessActivities = Some(List(sicCode)),
      mainBusinessActivity = Some(sicCode),
      labourCompliance = None
    )

    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
      .s4lContainer.clearedByKey
      .icl.fetchResults(List(sicCode))

    insertIntoDb(sessionId, iclSicCodeMapping)

    val fetchResultsResponse = buildClient("/save-sic-codes").get()
    whenReady(fetchResultsResponse) { res =>
      res.status mustBe SEE_OTHER
    }
  }

  "Returning from ICL with multiple SIC codes (non compliance) should fetch sic codes, save in keystore and return a SEE_OTHER" in new Setup {
    val sicCode1 = SicCode("23456", "This is a fake description", "")
    val sicCode2 = SicCode("12345", "This is another code", "")

    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .s4lContainer[Business].isUpdatedWith(fullModel.copy(businessActivities = Some(List(sicCode1, sicCode2))))
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .icl.fetchResults(List(sicCode1, sicCode2))

    insertIntoDb(sessionId, iclSicCodeMapping)

    val fetchResultsResponse = buildClient("/save-sic-codes").get()
    whenReady(fetchResultsResponse) { res =>
      res.status mustBe SEE_OTHER
    }
  }

  "Returning from ICL with multiple SIC codes (non compliance) should fetch sic codes, save in keystore and return a SEE_OTHER for SoleTrader" in new Setup {
    val sicCode1 = SicCode("23456", "This is a fake description", "")
    val sicCode2 = SicCode("12345", "This is another code", "")

    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .s4lContainer[Business].isUpdatedWith(fullModel.copy(businessActivities = Some(List(sicCode1, sicCode2))))
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .icl.fetchResults(List(sicCode1, sicCode2))

    insertIntoDb(sessionId, iclSicCodeMapping)

    val fetchResultsResponse = buildClient("/save-sic-codes").get()
    whenReady(fetchResultsResponse) { res =>
      res.status mustBe SEE_OTHER
    }
  }

  "Returning from ICL with a single SIC codes (compliance) should fetch sic codes, save in keystore and return a SEE_OTHER" in new Setup {
    val sicCode1 = SicCode("01610", "This is a compliance activity", "")

    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(modelWithoutCompliance)
      .s4lContainer[Business].isUpdatedWith(modelWithoutCompliance.copy(businessActivities = Some(List(sicCode1)), mainBusinessActivity = Some(sicCode1)))
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
      .icl.fetchResults(List(sicCode1))

    insertIntoDb(sessionId, iclSicCodeMapping)

    val fetchResultsResponse = buildClient("/save-sic-codes").get()
    whenReady(fetchResultsResponse) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.sicandcompliance.routes.BusinessActivitiesResolverController.resolve.url)
    }
  }


  "Workers should return OK on show and users answer is pre-popped on page" in new Setup {
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)
      .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/number-of-workers-supplied").get()
    whenReady(response) { res =>
      res.status mustBe OK
      val document = Jsoup.parse(res.body)
      document.getElementById("numberOfWorkers").attr("value") mustBe fullModel.labourCompliance.flatMap(_.numOfWorkersSupplied).get.toString
    }
  }

  "ComplianceIntroduction should return OK on show" in new Setup {
    given()
      .user.isAuthorised()

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/tell-us-more-about-the-business").get()
    whenReady(response) { res =>
      res.status mustBe 200
    }
  }

  "ComplianceIntroduction should return 303 for labour sic code on submit" in new Setup {
    val labourSicCode = "42110123"
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel.copy(
      mainBusinessActivity = Some(SicCode(labourSicCode, "", "")),
      businessActivities = Some(List(SicCode(labourSicCode, "", "")))
    ))

    insertCurrentProfileIntoDb(currentProfile, sessionId)

    val response = buildClient("/tell-us-more-about-the-business").post(Map("" -> Seq("")))
    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.SupplyWorkersController.show.url)
    }
  }

}