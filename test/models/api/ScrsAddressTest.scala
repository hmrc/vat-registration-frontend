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

package models.api

import org.scalatest._
import uk.gov.hmrc.play.test.UnitSpec

class ScrsAddressTest extends UnitSpec with Matchers {

  "equals" should {
    "match given same line1 and postcode" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None)
      val a2 = ScrsAddress("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None)
      a1 shouldBe a2
    }

    "not match given same line1 and different postcode" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None)
      val a2 = ScrsAddress("1 Address Line", "line 2", None, None, Some("AA2 1AA"), None)
      a1 should not equal a2
    }

    "not match given different line1 and same postcode" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, Some("AA1 1AA"), None)
      val a2 = ScrsAddress("9 Address Line", "line 2", None, None, Some("AA1 1AA"), None)
      a1 should not equal a2
    }

    "match given same line1 and country" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, None, Some("Slovakia"))
      val a2 = ScrsAddress("1 Address Line", "line 2", None, None, None, Some("Slovakia"))
      a1 shouldBe a2
    }

    "not match given same line1 and different country" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, None, Some("Slovakia"))
      val a2 = ScrsAddress("1 Address Line", "line 2", None, None, None, Some("Romania"))
      a1 should not equal a2
    }

    "not match given different line1 and same country" in {
      val a1 = ScrsAddress("1 Address Line", "line 2", None, None, None, Some("Slovakia"))
      val a2 = ScrsAddress("9 Address Line", "line 2", None, None, None, Some("Slovakia"))
      a1 should not equal a2
    }

  }

  "normalise" should {
    "leave normal address as-is" in {
      val a1 = ScrsAddress("line 1", "line 2", None, None, Some("postcode"), None)
      a1.normalise shouldBe a1

      val a2 = ScrsAddress("line 1", "line 2", Some("l3"), Some("l4"), None, Some("uk"))
      a2.normalise shouldBe a2
    }

    "convert empty some to none " in {
      val a1 = ScrsAddress("line 1", "line 2", Some(""), Some("    "), Some("  "), Some("UK"))
      val expected1 = ScrsAddress("line 1", "line 2", None, None, None, Some("UK"))
      a1.normalise shouldBe expected1

      val a2 = ScrsAddress("line 1", "line 2", Some(""), Some("  "), Some("postcode"), Some("   "))
      val expected2 = ScrsAddress("line 1", "line 2", None, None, Some("postcode"), None)
      a2.normalise shouldBe expected2
    }
  }
}
