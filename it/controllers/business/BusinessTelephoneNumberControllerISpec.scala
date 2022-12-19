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
import models.Business
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class BusinessTelephoneNumberControllerISpec extends ControllerISpec {
  val businessTelephoneNumber = "123456789"

  "show Business Telephone Number page" should {
    "return OK" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/business-telephone-number").get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

  "submit Business Telephone Number page" should {
    "return SEE_OTHER" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.replaceSection[Business](Business(telephoneNumber = Some("123456789")))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/business-telephone-number").post(Map("daytimePhone" -> Seq(businessTelephoneNumber)))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.business.routes.HasWebsiteController.show.url)
      }
    }

    "return BAD_REQUEST" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient("/business-telephone-number").post(Map("" -> Seq("")))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}