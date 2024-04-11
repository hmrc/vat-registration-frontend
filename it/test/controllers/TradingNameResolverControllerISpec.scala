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

import itutil.ControllerISpec
import models.api._
import models.{ApplicantDetails, Business}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class TradingNameResolverControllerISpec extends ControllerISpec {

  val url: String = controllers.routes.TradingNameResolverController.resolve.url

  "Trading name page resolver" should {
    List(Individual, NETP).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.business.routes.CaptureTradingNameController.show.url} for ${partyType.toString}" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.CaptureTradingNameController.show.url)
        }
      }
    }

    List(Partnership, ScotPartnership).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.business.routes.PartnershipNameController.show.url} for ${partyType.toString}" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.PartnershipNameController.show.url)
        }
      }
    }

    List(UkCompany, ScotLtdPartnership).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.business.routes.ShortOrgNameController.show.url} for a ${partyType.toString} with a company name longer than 105" in new Setup {
        val longCompanyName: String = "1" * 106

        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity =
          Some(testApplicantIncorpDetails.copy(companyName = Some(longCompanyName)))
        )))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ShortOrgNameController.show.url)
        }
      }
    }

    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.business.routes.ConfirmTradingNameController.show.url} for ${partyType.toString}" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.ConfirmTradingNameController.show.url)
        }
      }
    }

    s"return SEE_OTHER and redirects to ${controllers.business.routes.PartnershipNameController.show.url} for ${ScotLtdPartnership.toString} when no company name" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(ScotLtdPartnership)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = ScotLtdPartnership)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testPartnership.copy(companyName = None)))))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.PartnershipNameController.show.url)
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { partyType =>
      s"return SEE_OTHER and redirects to ${controllers.business.routes.BusinessNameController.show.url} for ${partyType.toString}" in new Setup {
        implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(partyType)
        given()
          .user.isAuthorised()
          .registrationApi.getSection[Business](None)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = partyType)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testMinorEntity))))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessNameController.show.url)
        }
      }
    }
  }
}
