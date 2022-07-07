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

package controllers

import common.enums.VatRegStatus
import fixtures.SicAndComplianceFixture
import helpers.RequestsFinder
import itutil.ControllerISpec
import models.Business.s4lKey
import models._
import models.api.{EligibilitySubmissionData, Individual, SicCode, VatScheme}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.test.Helpers._

class SicControllerISpec extends ControllerISpec with RequestsFinder with SicAndComplianceFixture {

  val sicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(currentProfile),
    ModelKeys.SIC_CODES_KEY -> Json.parse(jsonListSicCode)
  )

  val iclSicCodeMapping: Map[String, JsValue] = Map(
    "CurrentProfile" -> Json.toJson(currentProfile),
    "ICLFetchResultsUri" -> JsString("/fetch-results")
  )

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
      .vatScheme.has("sicAndComp", Json.parse(simplifiedSicJson))
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
      .vatScheme.doesNotHave("sicAndComp")
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
      .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
      .vatScheme.isUpdatedWith[Business](expectedUpdateToBusiness)
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
      .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData)
        )
      )
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
      .vatScheme.contains(
      VatScheme(id = currentProfile.registrationId,
        status = VatRegStatus.draft,
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
      )
    )
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
      .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
      .icl.fetchResults(List(sicCode1))

    insertIntoDb(sessionId, iclSicCodeMapping)

    val fetchResultsResponse = buildClient("/save-sic-codes").get()
    whenReady(fetchResultsResponse) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(business.routes.ComplianceIntroductionController.show.url)
    }
  }

  "MainBusinessActivity on show returns OK" in new Setup {
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(fullModel)

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(business.routes.SicController.showMainBusinessActivity.url).get()
    whenReady(response) { res =>
      res.status mustBe OK
    }
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete" in new Setup {
    val incompleteModelWithoutSicCode = fullModel.copy(mainBusinessActivity = None)
    val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(incompleteModelWithoutSicCode)
      .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData)
        )
      )
      .vatScheme.isUpdatedWith[Business](expectedUpdateToBusiness)
      .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
      .s4lContainer[Business].clearedByKey

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(business.routes.SicController.submitMainBusinessActivity.url).post(Map("value" -> Seq(sicCodeId)))
    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
    }
  }

  "MainBusinessActivity on submit returns SEE_OTHER vat Scheme is upserted because the model is NOW complete for SoleTrader" in new Setup {

    val incompleteModelWithoutSicCode: Business = fullModel.copy(mainBusinessActivity = None)
    val expectedUpdateToBusiness: Business = incompleteModelWithoutSicCode.copy(mainBusinessActivity = Some(mainBusinessActivity))
    given()
      .user.isAuthorised()
      .s4lContainer[Business].contains(incompleteModelWithoutSicCode)
      .vatScheme.contains(
        VatScheme(id = currentProfile.registrationId,
          status = VatRegStatus.draft,
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Individual))
        )
      )
      .vatScheme.isUpdatedWith[Business](expectedUpdateToBusiness)
      .registrationApi.replaceSection[Business](expectedUpdateToBusiness)
      .s4lContainer[Business].clearedByKey

    insertIntoDb(sessionId, sicCodeMapping)

    val response = buildClient(business.routes.SicController.submitMainBusinessActivity.url).post(Map("value" -> Seq(sicCodeId)))
    whenReady(response) { res =>
      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TradingNameResolverController.resolve.url)
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