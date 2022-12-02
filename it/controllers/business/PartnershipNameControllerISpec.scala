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
import models.api.{EligibilitySubmissionData, Partnership, Trust}
import models.{ApplicantDetails, Partner}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnershipNameControllerISpec extends ControllerISpec {
  val partnershipName = "testPartnershipName"

  "show Partnership Name page" must {
    List(Some(testCompanyName), None).foreach { companyName =>
      s"return OK for company name '$companyName'" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[ApplicantDetails](
          Some(validFullApplicantDetails.copy(entity = Some(testPartnership.copy(companyName = companyName))))
        )
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient("/partnership-official-name").get()
        whenReady(response) { res =>
          res.status mustBe OK
        }
      }
    }
  }

  "submit Partnership Name page" must {
    "return SEE_OTHER" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)
      val testApplicant = validFullApplicantDetails.copy(entity = Some(testPartnership), roleInTheBusiness = Some(Partner))
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(testApplicant))
        .registrationApi.replaceSection[ApplicantDetails](
        testApplicant.copy(
          entity = Some(testPartnership.copy(
            companyName = Some(partnershipName)
          ))
        ))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").post(Map("partnershipName" -> Seq(partnershipName)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.ConfirmTradingNameController.show.url)
      }
    }

    "return INTERNAL_SERVER_ERROR for invalid entity type" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Trust)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testMinorEntity))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Trust)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").post(Map("partnershipName" -> Seq(partnershipName)))
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return BAD_REQUEST for missing partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/partnership-official-name").post(Map("partnershipName" -> "a" * 106))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}