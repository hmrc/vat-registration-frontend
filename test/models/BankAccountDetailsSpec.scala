/*
 * Copyright 2024 HM Revenue & Customs
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

import models.bars.BankAccountType
import play.api.libs.json.{JsError, Json, JsSuccess}
import testHelpers.VatRegSpec

class BankAccountDetailsSpec extends VatRegSpec {

  "bankAccount" should {
    "parse successfully from Json when details is present" in {
      Json.toJson(validUkBankAccount).validate[BankAccount] mustBe JsSuccess(validUkBankAccount)
    }

    "parse successfully from Json when reason is present" in {
      val expected = validUkBankAccount.copy(details = None, reason = Some(BeingSetupOrNameChange))
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when rollNumber is present in details" in {
      val expected = validUkBankAccount.copy(details = Some(BankAccountDetails("testName", "12-34-56", "12345678", rollNumber = Some("AB/121212"))))
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when neither details or reason are present" in {
      val expected = validUkBankAccount.copy(details = None, reason = None)
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when bankAccountType is Business" in {
      val expected = validUkBankAccount.copy(bankAccountType = Some(BankAccountType.Business))
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when bankAccountType is Personal" in {
      val expected = validUkBankAccount.copy(bankAccountType = Some(BankAccountType.Personal))
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "parse successfully from Json when bankAccountType is absent" in {
      val expected = validUkBankAccount.copy(bankAccountType = None)
      Json.toJson(expected).validate[BankAccount] mustBe JsSuccess(expected)
    }

    "write bankAccountType as 'business' for Business" in {
      val json = Json.toJson(validUkBankAccount.copy(bankAccountType = Some(BankAccountType.Business)))
      (json \ "bankAccountType").as[String] mustBe "business"
    }

    "write bankAccountType as 'personal' for Personal" in {
      val json = Json.toJson(validUkBankAccount.copy(bankAccountType = Some(BankAccountType.Personal)))
      (json \ "bankAccountType").as[String] mustBe "personal"
    }

    "write bankAccountType from 'business' string" in {
      val json = Json.obj(
        "isProvided"      -> true,
        "bankAccountType" -> "business"
      )
      json.validate[BankAccount].map(_.bankAccountType) mustBe JsSuccess(Some(BankAccountType.Business))
    }

    "write bankAccountType from 'personal' string" in {
      val json = Json.obj(
        "isProvided"      -> true,
        "bankAccountType" -> "personal"
      )
      json.validate[BankAccount].map(_.bankAccountType) mustBe JsSuccess(Some(BankAccountType.Personal))
    }

    "fail to write bankAccountType from an unrecognised string" in {
      val json = Json.obj(
        "isProvided"      -> true,
        "bankAccountType" -> "unknown"
      )
      json.validate[BankAccount].map(_.bankAccountType) mustBe a[JsError]
    }

    "write rollNumber when present" in {
      val json = Json.obj(
        "isProvided" -> true,
        "details" -> Json.obj(
          "name"       -> "testName",
          "sortCode"   -> "12-34-56",
          "number"     -> "12345678",
          "rollNumber" -> "AB/121212"
        )
      )
      json.validate[BankAccount].map(_.details.flatMap(_.rollNumber)) mustBe JsSuccess(Some("AB/121212"))
    }
  }
}
