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

import models.BankAccountDetails
import models.api.ValidStatus
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.crypto.SymmetricCryptoFactory

class BankAccountDetailsSessionFormatSpec extends AnyWordSpec with Matchers {

  private val testEncryptionKey = "MTIzNDU2Nzg5MDEyMzQ1Ng=="
  private val encrypter         = SymmetricCryptoFactory.aesCryptoFromConfig(
    baseConfigKey = "json.encryption",
    config = com.typesafe.config.ConfigFactory.parseString(
      s"""json.encryption.key="$testEncryptionKey""""
    )
  )

  private implicit val format: Format[BankAccountDetails] =
    BankAccountDetailsSessionFormat.format(encrypter)

  val details: BankAccountDetails = BankAccountDetails(
    name       = "Test Account",
    sortCode   = "123456",
    number     = "12345678",
    rollNumber = None,
    status     = None
  )

  "BankAccountDetailsSessionFormat" should {

    "encrypt sortCode and number when writing to Json" in {
      val json = Json.toJson(details)
      (json \ "sortCode").as[String] mustNot equal("123456")
      (json \ "number").as[String] mustNot equal("12345678")
    }

    "not encrypt name when writing to Json" in {
      val json = Json.toJson(details)
      (json \ "name").as[String] mustBe "Test Account"
    }

    "not encrypt rollNumber when writing to Json" in {
      val withRollNumber = details.copy(rollNumber = Some("AB/121212"))
      val json           = Json.toJson(withRollNumber)
      (json \ "rollNumber").as[String] mustBe "AB/121212"
    }

    "rollNumber is not included from Json when None" in {
      val json = Json.toJson(details)
      (json \ "rollNumber").toOption mustBe None
    }

    "status not included from Json when None" in {
      val json = Json.toJson(details)
      (json \ "status").toOption mustBe None
    }

    "read and write BankAccountDetails with all fields" in {
      val full = BankAccountDetails(
        name       = "Test Account",
        sortCode   = "123456",
        number     = "12345678",
        rollNumber = Some("AB/121212"),
        status     = Some(ValidStatus)
      )
      val json   = Json.toJson(full)
      val result = Json.fromJson[BankAccountDetails](json).get
      result mustBe full
    }

    "fail to decrypt with a different key" in {
      val json = Json.toJson(details)

      val differentKey       = "ZGlmZmVyZW50a2V5MTIzNA=="
      val differentEncrypter = SymmetricCryptoFactory.aesCryptoFromConfig(
        baseConfigKey = "json.encryption",
        config = com.typesafe.config.ConfigFactory.parseString(
          s"""json.encryption.key="$differentKey""""
        )
      )
      val differentFormat: Format[BankAccountDetails] =
        BankAccountDetailsSessionFormat.format(differentEncrypter)

      Json.fromJson[BankAccountDetails](json)(differentFormat).isError mustBe true
    }
  }
}