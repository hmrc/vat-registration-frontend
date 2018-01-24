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

package features.businessContact.forms

import features.businessContact.models.CompanyContactDetails
import uk.gov.hmrc.play.test.UnitSpec

class CompanyContactDetailsFormSpec extends UnitSpec {

  val testForm = CompanyContactDetailsForm.form


  "A business contact details form" must {

    val EMAIL = "some@email.com"
    val DAYTIME_PHONE = "0123456789"
    val MOBILE = "9876543210"
    val WEBSITE = "http://www.some.website.com/"

    "be valid" when {

      "email and mobile number is provided" in {
        val data = Map("email" -> Seq(EMAIL), "mobileNumber" -> Seq(MOBILE))
        testForm.bindFromRequest(data).get shouldBe CompanyContactDetails(EMAIL, None, Some(MOBILE), None)
      }

      "email and phone number is provided" in {
        val data = Map("email" -> Seq(EMAIL), "phoneNumber" -> Seq(DAYTIME_PHONE))
        testForm.bindFromRequest(data).get shouldBe CompanyContactDetails(EMAIL, Some(DAYTIME_PHONE), None, None)
      }

      "email and both phone numbers are provided" in {
        val data = Map("email" -> Seq(EMAIL), "mobileNumber" -> Seq(MOBILE), "phoneNumber" -> Seq(DAYTIME_PHONE))
        testForm.bindFromRequest(data).get shouldBe CompanyContactDetails(
          email          = EMAIL,
          phoneNumber    = Some(DAYTIME_PHONE),
          mobileNumber   = Some(MOBILE),
          websiteAddress = None
        )
      }

      "all fields filled in" in {

        val data = Map(
          "email" -> Seq(EMAIL),
          "mobileNumber" -> Seq(MOBILE),
          "phoneNumber" -> Seq(DAYTIME_PHONE),
          "websiteAddress" -> Seq(WEBSITE))

        val form = testForm.bindFromRequest(data)

        form.get shouldBe CompanyContactDetails(EMAIL, Some(DAYTIME_PHONE), Some(MOBILE), Some(WEBSITE))
      }

      "any additional values are submitted" in {
        val data: Map[String, Seq[String]] = Map(
          "email" -> Seq(EMAIL),
          "phoneNumber" -> Seq(DAYTIME_PHONE),
          "foo" -> Seq("bar"))

        val form = testForm.bindFromRequest(data)

        form.get shouldBe CompanyContactDetails(EMAIL, Some(DAYTIME_PHONE), None, None)
      }

    }

    "be rejected with appropriate error messages" when {

      "no phone number is provided, just email" in {
        val data = Map("email" -> Seq(EMAIL))
        val form = testForm.bindFromRequest(data)
        form.globalErrors shouldBe Seq(
          "mobileNumber" -> "validation.businessContactDetails.mobile.missing",
          "phone"        -> "validation.businessContactDetails.daytimePhone.missing"
        )
      }

      "invalid mobile phone number is provided and email" in {
        val data = Map("email" -> Seq(EMAIL), "mobileNumber" -> Seq("invalid phone number"))
        val form = testForm.bindFromRequest(data)
        form.errors shouldBe Seq("mobileNumber" -> "validation.businessContactDetails.mobile.invalid")
      }

      "invalid daytime phone number is provided and email" in {
        val data = Map("email" -> Seq(EMAIL), "phoneNumber" -> Seq("invalid phone number"))
        val form = testForm.bindFromRequest(data)
        form.errors shouldBe Seq("phoneNumber" -> "validation.businessContactDetails.daytimePhone.invalid")
      }

      "no email is provided" in {
        val data = Map("phoneNumber" -> Seq(DAYTIME_PHONE))
        val form = testForm.bindFromRequest(data)
        form.errors shouldBe Seq("email" -> "validation.businessContactDetails.email.missing")
      }

      "blank email is provided" in {
        val data = Map("email" -> Seq(""), "mobileNumber" -> Seq(MOBILE))
        val form = testForm.bindFromRequest(data)
        form.errors shouldBe Seq("email" -> "validation.businessContactDetails.email.missing")
      }

      "invalid email is provided" in {
        val data = Map("email" -> Seq("some invalid email"), "phoneNumber" -> Seq(DAYTIME_PHONE))
        val form = testForm.bindFromRequest(data)
        form.errors shouldBe Seq("email" -> "validation.businessContactDetails.email.invalid")
      }
    }
  }
}
