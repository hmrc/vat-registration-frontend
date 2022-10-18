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

import itutil.ControllerISpec
import models.Entity
import models.api.{Individual, UkCompany}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartnerAddressControllerISpec extends ControllerISpec {

  def url(index: Int): String = routes.PartnerAddressController.redirectToAlf(index).url

  val testCallbackId = "testCallbackId"

  def callbackUrl(index: Int): String = routes.PartnerAddressController.addressLookupCallback(index, testCallbackId).url

  s"GET ${url(2)}" should {
    "redirect to ALF with sole trader config" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
      }
    }

    "redirect to ALF with company config" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(Some(testIncorpDetails), UkCompany, Some(false), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
      }
    }

    "redirect to first entity loop page if entity is missing entirely" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }

    "redirect to first entity loop page if entity is missing business details" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getListSection[Entity](Some(List(
        Entity(Some(testIncorpDetails), UkCompany, Some(true), None, None, None, None),
        Entity(None, UkCompany, Some(false), None, None, None, None)
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url(2)).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.PartnerEntityTypeController.showPartnerType(2).url)
      }
    }
  }

  s"GET ${callbackUrl(2)}" should {
    "return SEE_OTHER and save the updated entity model" in new Setup {
      val testEntity: Entity = Entity(Some(testSoleTrader), Individual, Some(false), None, None, None, None)
      given()
        .user.isAuthorised()
        .address(testCallbackId, testLine1, testLine2, "UK", "XX XX").isFound
        .registrationApi.getSection[Entity](Some(testEntity), testRegId, Some(2))
        .registrationApi.replaceSection[Entity](testEntity.copy(address = Some(addressWithCountry)), testRegId, Some(2))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(callbackUrl(2)).get()

      whenReady(response) { res =>
        res.status mustBe NOT_IMPLEMENTED
      }
    }
  }

}
