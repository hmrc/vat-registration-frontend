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

package controllers.business

import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, NonUkNonEstablished, UkCompany}
import models.{ApplicantDetails, TradingDetails, TradingNameView}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.test.Helpers._

class TradingNameControllerISpec extends ControllerISpec {
  val companyName = "testCompanyName"

  "show Trading Name page" should {
    "return OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].isEmpty
        .s4lContainer[ApplicantDetails].isUpdatedWith(validFullApplicantDetails)
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .vatScheme.doesNotHave("trading-details")
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Trading Name page" should {
    "return SEE_OTHER with redirect to Imports or Exports" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .vatScheme.isUpdatedWith(tradingDetails.copy(tradingNameView = Some(TradingNameView(true, Some("Test Trading Name")))))
        .s4lContainer[TradingDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post(Map("value" -> Seq("true"), "tradingName" -> Seq("Test Trading Name")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ImportsOrExportsController.show.url)
      }
    }

    "return SEE_OTHER with redirect to Turnover Estimate for non UK registrations" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .vatScheme.isUpdatedWith(tradingDetails.copy(tradingNameView = Some(TradingNameView(false))))
        .s4lContainer[TradingDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NonUkNonEstablished)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post(Map("value" -> Seq("false")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.returns.routes.TurnoverEstimateController.show.url)
      }
    }

    "return BAD_REQUEST if true is selected without a name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post(Map("value" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST if nothing is selected" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .vatScheme.doesNotHave("trading-details")
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient("/trading-name").post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}