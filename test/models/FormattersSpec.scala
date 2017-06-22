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

package models

import org.scalatest.Inspectors
import play.api.libs.json.{JsArray, JsString, JsSuccess}
import uk.gov.hmrc.play.test.UnitSpec

class FormattersSpec extends UnitSpec with Inspectors {


  "NINO" should {
    "be formatted correctly" in {
      Formatters.ninoFormatter("AB123456X") shouldBe "AB 12 34 56 X"
    }
  }

  "String reads" should {
    "be normalized" in {
      Formatters.normalizeReads.reads(JsString("Peter Perháč")) shouldBe JsSuccess("Peter Perhac")
    }
  }

  "List[String] reads" should {
    "be normalized" in {
      val jsonStringArray = JsArray(Seq(JsString("Peter Perháč"), JsString("Perháč Peter")))
      Formatters.normalizeListReads.reads(jsonStringArray) shouldBe JsSuccess(Seq("Peter Perhac", "Perhac Peter"))
    }
  }

}
