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

import play.api.libs.json.{Format, Json}

case class OverseasBankDetails(name: String,
                               bic: String,
                               iban: String)

object OverseasBankDetails {
  implicit val format: Format[OverseasBankDetails] = Json.format[OverseasBankDetails]

  def overseasBankSeq(overseasBankAccount: OverseasBankDetails): Seq[String] = {
    Seq(
      overseasBankAccount.name,
      overseasBankAccount.bic,
      overseasBankAccount.iban
    )
  }
}
