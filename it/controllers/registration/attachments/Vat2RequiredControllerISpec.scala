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

package controllers.registration.attachments

import itutil.ControllerISpec
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.OK

import scala.concurrent.Future

class Vat2RequiredControllerISpec extends ControllerISpec{

  val url: String = controllers.registration.attachments.routes.Vat2RequiredController.show.url

  s"GET $url" must {
    "return an OK" in new Setup {
      given()
        .user.isAuthorised()
        .audit.writesAudit()
        .audit.writesAuditMerged()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }
  }

}
