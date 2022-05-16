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
import models.ApplicantDetails
import models.api.{EligibilitySubmissionData, Trust, UkCompany}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessNameControllerISpec extends ControllerISpec {
  val businessName = "testBusinessName"

  "show Business Name page" should {
    "return OK" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .s4lContainer[ApplicantDetails].isEmpty
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/business-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Business Name page" should {
    "return SEE_OTHER" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Trust)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testMinorEntity))))
        .registrationApi.replaceSection[ApplicantDetails](
        validFullApplicantDetails.copy(
          entity = Some(testMinorEntity.copy(
            companyName = Some(businessName)
          ))
        ))
        .s4lContainer[ApplicantDetails].isEmpty
        .s4lContainer[ApplicantDetails].clearedByKey
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Trust)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/business-name").post(Map("businessName" -> Seq(businessName)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.TradingNameController.show.url)
      }
    }
  }
}