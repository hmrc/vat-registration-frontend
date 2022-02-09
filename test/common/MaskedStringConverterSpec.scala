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

package common

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{Inside, Inspectors}
import org.scalatest.matchers.should.Matchers


class MaskedStringConverterSpec extends AnyWordSpec with Inside with Inspectors with Matchers {

  import StringMasking._

  "mask" must {
    //masking with § character on purpose, to show it can accept non-* masking character explicitly
    "mask first n-characters of a string if string is longer than n (or n-characters long)" in {
      val str = "1234567890"
      forAll(1 to 10) {
        n => str.mask(n, '§').count(_ == '§') shouldBe n
      }
    }

    "mask all characters of a string if n > length of string" in {
      val str = "foo"
      forAll(4 to 10) {
        n => str.mask(n, '§').count(_ == '§') shouldBe str.length
      }
    }
  }
}
