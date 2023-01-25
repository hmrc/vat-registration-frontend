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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig}
import itutil.ControllerISpec
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import services.SessionService
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BaseControllerISpec extends ControllerISpec {

  val messageKey = "service.name"
  val testMessage = "Register for VAT"
  val testWelshMessage = "Cofrestru ar gyfer TAW"

  val controller: TestController = app.injector.instanceOf[TestController]

  val enRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "en"))
  val cyRequest = FakeRequest().withCookies(Cookie("PLAY_LANG", "cy"))

  "request2Messages" when {
    "the welsh FS" must {
      "return english text if he user has an english language cookie" in {
        controller.request2Messages(enRequest)("service.name") mustBe testMessage
      }
      "return english text if he user has an welsh language cookie" in {
        controller.request2Messages(cyRequest)("service.name") mustBe testWelshMessage
      }
    }
  }
}

class TestController @Inject()(val authConnector: AuthConnector,
                               val sessionService: SessionService)
                              (implicit appConfig: FrontendAppConfig,
                               val executionContext: ExecutionContext,
                               baseControllerComponents: BaseControllerComponents) extends BaseController