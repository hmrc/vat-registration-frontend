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
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames

import scala.concurrent.Future

class HasWebsiteControllerISpec extends ControllerISpec {

  val url = "/business-has-website"

  "GET /business-has-website" must {
    "return OK with a blank form if no data is stored" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'Yes' pre-populated" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
    "return OK with 'No' pre-populated" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "POST /business-has-website" must {
    "redirect to business website capture page if yes is chosen" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(hasWebsite = Some(true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "true")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)
    }

    "redirect to vat correspondence page if no is chosen" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[Business](None)
        .registrationApi.replaceSection[Business](Business(hasWebsite = Some(false)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map("value" -> "false")))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.VatCorrespondenceController.show.url)
    }

    "return BAD_REQUEST if no option is selected" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}