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

import itutil.ControllerISpec
import models.Business
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PpobAddressControllerISpec extends ControllerISpec {

  "GET /principal-place-business" should {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.business.routes.PpobAddressController.startJourney.url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
      }
    }
    "return INTERNAL_SERVER_ERROR when not authorised" in new Setup {
      given()
        .user.isNotAuthorised

      val response: Future[WSResponse] = buildClient(controllers.business.routes.PpobAddressController.startJourney.url).get()
      whenReady(response) { res =>
        res.status mustBe INTERNAL_SERVER_ERROR

      }
    }
  }

  "GET /principal-place-business/acceptFromTxm" should {
    "return SEE_OTHER save to vat as model is complete" in new Setup {
      given()
        .user.isAuthorised()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .registrationApi.replaceSection[Business](businessDetails, testRegId)(Business.apiKey, Business.format)
        .registrationApi.getSection[Business](Some(businessDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessEmailController.show.url)
      }

    }
    "returnFromTxm should return SEE_OTHER save to backend as model is incomplete" in new Setup {
      given()
        .user.isAuthorised()
        .address("fudgesicle", testLine1, testLine2, "UK", "XX XX").isFound
        .registrationApi.getSection[Business](None, testRegId)
        .registrationApi.replaceSection[Business](Business(ppobAddress = Some(addressWithCountry)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(controllers.business.routes.PpobAddressController.callback(id = "fudgesicle").url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessEmailController.show.url)
      }
    }
  }

}
