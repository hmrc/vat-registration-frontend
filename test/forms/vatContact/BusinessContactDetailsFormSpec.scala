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

package forms.vatContact

import org.scalatest.{Inside, Inspectors, Matchers}
import testHelpers.FormInspectors
import uk.gov.hmrc.play.test.UnitSpec

class BusinessContactDetailsFormSpec extends UnitSpec with Inspectors with Matchers with Inside with FormInspectors {

  val testForm = BusinessContactDetailsForm.form


  "A business contact details form" must {

    "be valid" when {

      "email and mobile number is provided" in {
        val data = Map("email" -> "some@email.com", "mobile" -> "01234567890")
        testForm.bind(data).errors shouldBe 'empty
      }

      "email and phone number is provided" in {
        val data = Map("email" -> "some@emai.com", "daytimePhone" -> "124124124")
        testForm.bind(data).errors shouldBe 'empty
      }

      "email and both phone numbers are provided" in {
        val data = Map("email" -> "some@emai.com", "mobile" -> "12124124", "daytimePhone" -> "124124124")
        testForm.bind(data).errors shouldBe 'empty
      }

      "all fields filled in" in {
        val data = Map(
          "email" -> "some@emai.com",
          "mobile" -> "12124124",
          "daytimePhone" -> "124124124",
          "website" -> "http://www.some.website.com/")
        val form = testForm.bind(data)

        form.errors shouldBe 'empty
        form("website").value shouldBe Some("http://www.some.website.com/")
      }

      "any additional values are submitted" in {
        val data = Map("email" -> "some@emai.com", "mobile" -> "12124124", "daytimePhone" -> "124124124", "foo" -> "bar")
        testForm.bind(data).errors shouldBe 'empty
      }

    }

    "be rejected with appropriate error messages" when {

      "no phone number is provided, just email" in {
        val data = Map("email" -> "some@email.com")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq(
          "daytimePhone" -> "validation.businessContactDetails.daytimePhone.missing",
          "mobile" -> "validation.businessContactDetails.mobile.missing"
        )
      }


      "invalid mobile phone number is provided and email" in {
        val data = Map("email" -> "some@email.com", "mobile" -> "invalid phone number")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq("mobile" -> "validation.businessContactDetails.mobile.invalid")
      }


      "invalid daytime phone number is provided and email" in {
        val data = Map("email" -> "some@email.com", "daytimePhone" -> "invalid phone number")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq("daytimePhone" -> "validation.businessContactDetails.daytimePhone.invalid")
      }

      "no email is provided" in {
        val data = Map("daytimePhone" -> "0123456789")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq("email" -> "validation.businessContactDetails.email.missing")
      }


      "blank email is provided" in {
        val data = Map("email" -> "", "daytimePhone" -> "0123456789")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq("email" -> "validation.businessContactDetails.email.missing")
      }

      "invalid email is provided" in {
        val data = Map("email" -> "some invalid email", "daytimePhone" -> "0123456789")
        val form = testForm.bind(data)
        form shouldHaveErrors Seq("email" -> "validation.businessContactDetails.email.invalid")
      }


    }

  }


}
