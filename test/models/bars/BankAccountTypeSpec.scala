/*
 * Copyright 2026 HM Revenue & Customs
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

package models.bars

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BankAccountTypeSpec extends AnyWordSpec with Matchers {

  "BankAccountType" when {

    "asBars" should {

      "return 'personal' string for Personal type bank account" in {
        BankAccountType.Personal.asBars shouldBe "personal"
      }

      "return 'business' string for Business type bank account" in {
        BankAccountType.Business.asBars shouldBe "business"
      }
    }

    "fromString" should {

      "return Some(Personal) for 'personal' string" in {
        BankAccountType.fromString("personal") shouldBe Some(BankAccountType.Personal)
      }

      "return Some(Business) for 'business' string" in {
        BankAccountType.fromString("business") shouldBe Some(BankAccountType.Business)
      }

      "return Some(Personal) with case-insensitive personal string" in {
        BankAccountType.fromString("PerSoNAL") shouldBe Some(BankAccountType.Personal)
      }

      "return Some(Business) with case-insensitive business string" in {
        BankAccountType.fromString("BusInESS") shouldBe Some(BankAccountType.Business)
      }

      "return None for an unrecognised string" in {
        BankAccountType.fromString("corporate") shouldBe None
      }

      "return None for an empty string" in {
        BankAccountType.fromString("") shouldBe None
      }
    }
  }
}
