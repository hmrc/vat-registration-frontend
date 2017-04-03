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

import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class ElementPathSpec extends UnitSpec  {

  val format  = ElementPath.ElementPathFormatter

  "ElementPathFormatter" should {

    "writes should return name" in {
      format.writes(VatBankAccountPath) shouldBe JsString(VatBankAccountPath.name)
      format.writes(ZeroRatedTurnoverEstimatePath) shouldBe JsString(ZeroRatedTurnoverEstimatePath.name)
      format.writes(AccountingPeriodStartPath) shouldBe JsString(AccountingPeriodStartPath.name)
    }

    "reads should return elementPath" in {
      format.reads(JsString(VatBankAccountPath.name)) shouldBe JsSuccess(VatBankAccountPath)
      format.reads(JsString(ZeroRatedTurnoverEstimatePath.name)) shouldBe JsSuccess(ZeroRatedTurnoverEstimatePath)
      format.reads(JsString(AccountingPeriodStartPath.name)) shouldBe JsSuccess(AccountingPeriodStartPath)
    }

    "reads should return error for unknown name" in {
      format.reads(JsString("Junk")) shouldBe JsError("unrecognised element name")
    }
  }
}