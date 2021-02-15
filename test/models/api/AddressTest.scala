/*
 * Copyright 2021 HM Revenue & Customs
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

package models.api

import testHelpers.VatRegSpec

class AddressTest extends VatRegSpec {

  "equals" should {
    "match given same line1 and postcode" in {
      val a1 = Address("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None, addressValidated = true)
      val a2 = Address("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None, addressValidated = true)
      a1 mustBe a2
    }

    "not match given same line1 and different postcode" in {
      val a1 = Address("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None, addressValidated = true)
      val a2 = Address("1 Address Line", "line 2", None, None, Some("AA2 1AA"), None, addressValidated = true)
      a1 must not equal a2
    }

    "not match given different line1 and same postcode" in {
      val a1 = Address("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None, addressValidated = true)
      val a2 = Address("9 Address Line", "line 2", None, None, Some("AA1 1AA"), None, addressValidated = true)
      a1 must not equal a2
    }

    "match given same line1 and country" in {
      val a1 = Address("1 Address Line", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      val a2 = Address("1 Address Line", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      a1 mustBe a2
    }

    "not match given same line1 and different country" in {
      val a1 = Address("1 Address Line", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      val a2 = Address("1 Address Line", "line 2", None, None, None, Some(Country(None, Some("Romania"))), addressValidated = true)
      a1 must not equal a2
    }

    "not match given different line1 and same country" in {
      val a1 = Address("1 Address Line", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      val a2 = Address("9 Address Line", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      a1 must not equal a2
    }

  }

  "normalise" should {
    "leave normal address as-is" in {
      val a1 = Address("line 1", "line 2", None, None, Some("postcode"), None, addressValidated = true)
      a1.normalise mustBe a1

      val a2 = Address("line 1", "line 2", Some("l3"), Some("l4"), None, Some(testCountry), addressValidated = true)
      a2.normalise mustBe a2
    }

    "convert empty some to none " in {
      val a1 = Address("line 1", "line 2", Some(""), Some("    "), Some("  "), Some(testCountry), addressValidated = true)
      val expected1 = Address("line 1", "line 2", None, None, None, Some(testCountry), addressValidated = true)
      a1.normalise mustBe expected1

      val a2 = Address("line 1", "line 2", Some(""), Some("  "), Some("postcode"), Some(Country(Some(""), Some("   "))), addressValidated = true)
      val expected2 = Address("line 1", "line 2", None, None, Some("postcode"), None, addressValidated = true)
      a2.normalise mustBe expected2
    }
  }
}
