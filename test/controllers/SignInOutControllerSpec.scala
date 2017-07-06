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

package controllers

import controllers.callbacks.SignInOutController
import helpers.VatRegSpec

class SignInOutControllerSpec extends VatRegSpec {

  object TestController extends SignInOutController(ds) {
    override val authConnector = mockAuthConnector
  }

  "Post-sign-in" should {
    "redirect to CT post sign in" in {
      callAuthorised(TestController.postSignIn) {
        _ redirectsTo s"${TestController.compRegFEURL}${TestController.compRegFEURI}/post-sign-in"
      }
    }
  }

  "signOut" should {
    "redirect to the exit questionnaire and clear the session" in {
      callAuthorised(TestController.signOut) {
        _ redirectsTo s"${TestController.compRegFEURL}${TestController.compRegFEURI}/questionnaire"
      }
    }
  }

}
