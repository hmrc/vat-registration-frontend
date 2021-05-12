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

package controllers.registration.returns

import controllers.registration.returns.{routes => annualAccountingRoutes}
import forms.AnnualStaggerForm.januaryKey
import itutil.ControllerISpec
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class LastMonthOfAccountingYearControllerISpec extends ControllerISpec {

  val url: String = annualAccountingRoutes.LastMonthOfAccountingYearController.show().url

  s"GET $url" must {
    "return an OK" in new Setup { //TODO add test with prepop
      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe 200
      }
    }
  }

  s"POST $url" must {
    "return a redirect to next page" in new Setup { //TODO Update to store and redirect to correct page
      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Map("value" -> januaryKey))

      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(annualAccountingRoutes.LastMonthOfAccountingYearController.show().url)
      }
    }
  }
}
