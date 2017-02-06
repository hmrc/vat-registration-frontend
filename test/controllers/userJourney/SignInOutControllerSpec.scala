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

package controllers.userJourney

import enums.DownstreamOutcome
import helpers.VatRegSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.VatRegistrationService

import scala.concurrent.Future

class SignInOutControllerSpec extends VatRegSpec {

  object TestController extends SignInOutController(ds) {
    override val authConnector = mockAuthConnector
  }

  "Post-sign-in" should {

    "redirect to start of the journey when authorized" in {
      callAuthorised(TestController.postSignIn, mockAuthConnector) {
        result =>
          status(result) mustBe SEE_OTHER
          inside(redirectLocation(result)) {
            case Some(redirectUri) => redirectUri mustBe routes.WelcomeController.start().toString
          }
      }
    }

  }


}
