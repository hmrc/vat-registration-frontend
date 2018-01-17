/*
 * Copyright 2018 HM Revenue & Customs
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

import helpers.{S4LMockSugar, VatRegSpec}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}

case class TestClass(text: String, number: Int)


class VatRegistrationControllerSpec extends VatRegSpec with S4LMockSugar {

  import testHelpers.FormInspectors._

  object TestController extends VatRegistrationController(ds) {
    override val authConnector = mockAuthConnector
    def authorisedActionGenerator: Action[AnyContent] = authorised { u => r => NoContent }

  }

  val testConstraint: Constraint[TestClass] = Constraint {
    case TestClass(t, n) if t.length < 5 && n > 20 => Invalid(ValidationError("message.code", "text"))
    case _ => Valid
  }

  val testForm = Form(
    mapping(
      "text" -> text(),
      "number" -> number()
    )(TestClass.apply)(TestClass.unapply).verifying(testConstraint)
  )

  "unauthorised access" should {
    "redirect user to GG sign in page" in {
      TestController.authorisedActionGenerator(FakeRequest()) redirectsTo authUrl
    }
  }

  "authorised access" should {
    "return success status" in {
      callAuthorised(TestController.authorisedActionGenerator)(status(_) mustBe NO_CONTENT)
    }
  }

  "copyGlobalErrorsToFields" should {

    "leave form object intact if no form errors are present" in {
      lazy val formUpdate = TestController.copyGlobalErrorsToFields[TestClass]("text")

      val data = Map(
        "text" -> Seq("some text"),
        "number" -> Seq("123")
      )
      val form = testForm.bindFromRequest(data)
      formUpdate(form) mustBe form
    }

    "leave form object intact when not interested in text field" in {
      lazy val formUpdate = TestController.copyGlobalErrorsToFields[TestClass]("number")

      val data = Map(
        "text" -> Seq("foo"),
        "number" -> Seq("123")
      )
      val form = testForm.bindFromRequest(data)
      formUpdate(form) mustBe form
    }

    "register an additional form field error on the 'text' field" in {
      lazy val formUpdate = TestController.copyGlobalErrorsToFields[TestClass]("text")

      val data = Map(
        "text" -> Seq("foo"),
        "number" -> Seq("123")
      )
      val form = testForm.bindFromRequest(data)
      formUpdate(form) shouldHaveErrors Seq("" -> "message.code", "text" -> "message.code")
    }
  }
}
