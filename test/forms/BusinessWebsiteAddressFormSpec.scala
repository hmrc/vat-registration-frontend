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

package forms

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}

class BusinessWebsiteAddressFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val businessWebsiteAddressForm: Form[String] = BusinessWebsiteAddressForm.form
  val testWebsiteAddress: String = "https://www.example.com"
  val incorrectFormatErrorKey: String = "validation.businessWebsiteAddress.invalid"
  val emptyErrorKey: String = "validation.businessWebsiteAddress.missing"
  val maxLengthErrorKey: String = "validation.businessWebsiteAddress.maxlen"

  "The businessWebsiteAddressForm" must {
    "validate that testWebsiteAddress is valid" in {
      val form = businessWebsiteAddressForm.bind(Map(BusinessWebsiteAddressForm.businessWebsiteAddressKey -> testWebsiteAddress)).value

      form mustBe Some(testWebsiteAddress)
    }

    "validate that the incorrect website address format fails" in {
      val formWithError = businessWebsiteAddressForm.bind(Map(BusinessWebsiteAddressForm.businessWebsiteAddressKey -> "invalid"))

      formWithError.errors must contain(FormError(BusinessWebsiteAddressForm.businessWebsiteAddressKey, incorrectFormatErrorKey))
    }

    "validate that website address exceeding max length fails" in {
      val exceedMaxLengthWebsiteAddress: String = ("a" * 132) + "test.com"
      val formWithError = businessWebsiteAddressForm.bind(Map(BusinessWebsiteAddressForm.businessWebsiteAddressKey -> exceedMaxLengthWebsiteAddress))

      formWithError.errors must contain(FormError(BusinessWebsiteAddressForm.businessWebsiteAddressKey, maxLengthErrorKey))
    }

    "validate that an empty field fails" in {
      val formWithError = businessWebsiteAddressForm.bind(Map(BusinessWebsiteAddressForm.businessWebsiteAddressKey -> ""))

      formWithError.errors must contain(FormError(BusinessWebsiteAddressForm.businessWebsiteAddressKey, emptyErrorKey))
    }
  }

}
