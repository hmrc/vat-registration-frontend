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

import models.api.BankAccountDetailsStatus
import models.BankAccountDetails
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption

object BankAccountDetailsSessionFormat {

  def format(encrypter: Encrypter with Decrypter): Format[BankAccountDetails] = {
    implicit val crypto: Encrypter with Decrypter = encrypter

    implicit val sensitiveStringFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    (
      (__ \ "name").format[String] and
        (__ \ "sortCode").format[SensitiveString]
          .bimap(_.decryptedValue, SensitiveString.apply) and
        (__ \ "number").format[SensitiveString]
          .bimap(_.decryptedValue, SensitiveString.apply) and
        (__ \ "rollNumber").formatNullable[String] and
        (__ \ "status").formatNullable[BankAccountDetailsStatus]
      )(BankAccountDetails.apply, unlift(BankAccountDetails.unapply))
  }
}