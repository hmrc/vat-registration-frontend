/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import fixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.api._
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class HonestyDeclarationControllerISpec extends ControllerISpec with ITRegistrationFixtures with FeatureSwitching {

  val url: String = controllers.routes.HonestyDeclarationController.show.url

  val userId = "user-id-12345"

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }

  s"POST $url" must {
    List(UkCompany, RegSociety, CharitableOrg).foreach { validPartyType =>
      s"return a redirect to Incorp ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Json.obj())
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.IncorpIdController.startJourney.url)
        }
      }
    }

    List(Partnership).foreach { validPartyType =>
      s"return a redirect to Partnership ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Json.obj())
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PartnershipIdController.startJourney.url)
        }
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { validPartyType =>
      s"return a redirect to Minor Entity ID for ${validPartyType.toString}" in new Setup {
        given()
          .user.isAuthorised
          .audit.writesAudit()
          .audit.writesAuditMerged()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = validPartyType)))
          .vatRegistration.honestyDeclaration(testRegId, "true")

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val response: Future[WSResponse] = buildClient(url).post(Json.obj())
        whenReady(response) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        }
      }
    }

    "return a redirect to STI if user is a sole trader" in new Setup {
      enable(UseSoleTraderIdentification)
      given()
        .user.isAuthorised
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Individual)))
        .vatRegistration.honestyDeclaration(testRegId, "true")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Json.obj())
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
      }
      disable(UseSoleTraderIdentification)
    }
  }
}
