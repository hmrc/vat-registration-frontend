/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.partners

import forms.PartnerTelephoneForm
import itutil.ControllerISpec
import models.Entity
import models.api.{EligibilitySubmissionData, Individual, UkCompany}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class PartnerTelephoneNumberControllerISpec extends ControllerISpec {

  def url(index: Int): String = routes.PartnerTelephoneNumberController.show(index).url

  private val testTelephone = "12345678"

  s"GET ${url(2)}" must {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "return OK with pre-pop for existing data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, Some(testTelephone))
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe OK
        Jsoup.parse(res.body).getElementById("partnerTelephone").attr("value") mustBe testTelephone
      }
    }

    "redirect to partner type page when entity name does not exist" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testSoleTrader), Individual, Some(true), None, None, None, None),
        Entity(Some(testIncorpDetails.copy(companyName = None)), UkCompany, Some(false), None, None, None, Some(testTelephone))
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.partners.routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }
  }

  s"POST ${url(2)}" must {
    "update the backend and redirect to partner email address page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[Entity](Some(Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)), idx = Some(2))
        .registrationApi.replaceSection[Entity](Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, Some(testTelephone)), idx = Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2))
        .post(Map(PartnerTelephoneForm.partnerTelephoneKey -> testTelephone))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.partners.routes.PartnerCaptureEmailAddressController.show(2).url)
      }
    }

    "return BAD_REQUEST for missing telephone" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2))
        .post("")

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid telephone" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2))
        .post(Map(PartnerTelephoneForm.partnerTelephoneKey -> "a" * 106))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
