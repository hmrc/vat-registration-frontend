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

import forms.ScottishPartnershipNameForm
import itutil.ControllerISpec
import models.api.{EligibilitySubmissionData, Partnership, ScotPartnership}
import models.{ApplicantDetails, Entity}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.Future

class ScottishPartnershipNameControllerISpec extends ControllerISpec {
  "show Scottish Partnership Name page" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[Entity](Some(Entity(Some(testPartnership), ScotPartnership, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "with pre-pop for existing data and return OK" in new Setup {
      private val scottishPartnershipName = "updated name"

      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), ScotPartnership, Some(true), Some(scottishPartnershipName), None, None, None)), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").get()
      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("scottishPartnershipName").attr("value") mustBe scottishPartnershipName
      }
    }
  }

  "submit Scottish Partnership Name page" should {
    "post to the backend" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)

      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(entity = Some(testPartnership))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionDataPartner))
        .registrationApi.getSection[Entity](Some(Entity(None, ScotPartnership, Some(true), None, None, None, None)), idx = Some(1))
        .registrationApi.replaceSection[Entity](Entity(None, ScotPartnership, Some(true), Some(testCompanyName), None, None, None), idx = Some(1))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient("/scottish-partnership-name").post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> testCompanyName)))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.grs.routes.PartnerPartnershipIdController.startJourney(1).url)
    }

    "return BAD_REQUEST for missing partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid partnership name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient("/scottish-partnership-name").post(Map(ScottishPartnershipNameForm.scottishPartnershipNameKey -> "a" * 106))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
