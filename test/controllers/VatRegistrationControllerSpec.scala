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

import helpers.VatRegSpec
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status, _}


class VatRegistrationControllerSpec extends VatRegSpec {

  object TestController extends VatRegistrationController(ds) {
    override val authConnector = mockAuthConnector
    def authorisedActionGenerator: Action[AnyContent] = authorised { u => r => NoContent }
  }

  "unauthorised access" should {
    "redirect user to GG sign in page" in {
      val result = TestController.authorisedActionGenerator(FakeRequest())
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(authUrl)
    }

  }


  "authorised access" should {
    "return success status" in {
      callAuthorised(TestController.authorisedActionGenerator, mockAuthConnector)(status(_) mustBe NO_CONTENT)
    }
  }

}
