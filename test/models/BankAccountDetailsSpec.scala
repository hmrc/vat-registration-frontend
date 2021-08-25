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

package models

import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec

class BankAccountDetailsSpec extends VatRegSpec {

  "bankAccount" should {
    "parse successfully from Json when details is present" in {
      Json.toJson(validUkBankAccount).validate[BankAccount] mustBe JsSuccess(validUkBankAccount)
    }

    "parse successfully from Json when reason is present" in {
      val expected = validUkBankAccount.copy(details = None, reason = Some(BeingSetup))
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when neither details or reason are present" in {
      val expected = validUkBankAccount.copy(details = None, reason = None)
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }
  }

}
