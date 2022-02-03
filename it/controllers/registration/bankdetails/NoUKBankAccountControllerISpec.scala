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

package controllers.registration.bankdetails

import itutil.ControllerISpec
import models.{BankAccount, BeingSetup}
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class NoUKBankAccountControllerISpec extends ControllerISpec {

  val url: String = routes.NoUKBankAccountController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, None))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200
      }
    }

    "return an OK with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, Some(BeingSetup)))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200

        Jsoup.parse(res.body).getElementById("value").attr("value") mustBe "beingSetup"
      }
    }
  }

  s"POST $url" must {
    "return a redirect to flatrate scheme" in new Setup {
      given()
        .user.isAuthorised()
        .s4lContainer[BankAccount].contains(BankAccount(isProvided = false, None, None, None))
        .vatScheme.isUpdatedWith[BankAccount](BankAccount(isProvided = false, None, None, Some(BeingSetup)))
        .s4lContainer[BankAccount].clearedByKey

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> "beingSetup"))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
      }
    }
  }
}
