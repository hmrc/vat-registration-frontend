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

import itutil.IntegrationSpecBase
import org.scalatest.concurrent.ScalaFutures
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import support.AppAndStubs

import scala.concurrent.Future
import controllers.registration.applicant.{routes => applicantRoutes}

class HonestyDeclarationControllerISpec extends IntegrationSpecBase with AppAndStubs with ScalaFutures {

  val url: String = controllers.routes.HonestyDeclarationController.show().url

  val userId = "user-id-12345"

  s"GET $url" must {
    "return an OK" in new StandardTestHelpers {
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
    "return a redirect to Incorp ID" in new StandardTestHelpers {
      given()
        .user.isAuthorised
        .audit.writesAudit()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).post(Json.obj())
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.IncorpIdController.startIncorpIdJourney().url)
      }
    }
  }
}
