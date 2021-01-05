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

package models.api

import play.api.libs.json._

sealed trait CustomerStatus {
  val value: String
}

case object MTDfBExempt extends CustomerStatus {
  override val value: String = "1"
}

case object MTDfB extends CustomerStatus {
  override val value: String = "2"
}

case object NonMTDfB extends CustomerStatus {
  override val value: String = "3"
}

case object NonDigital extends CustomerStatus {
  override val value: String = "4"
}


object CustomerStatus {

  def unapply(arg: CustomerStatus): Option[String] = Some(arg.value)

  val reads: Reads[CustomerStatus] = for {
    value <- JsPath.read[String].map {
      case MTDfB.value => MTDfB
      case MTDfBExempt.value => MTDfBExempt
      case NonMTDfB.value => NonMTDfB
      case NonDigital.value => NonDigital
    }
  } yield value

  val writes: Writes[CustomerStatus] = Writes {
    status: CustomerStatus => JsString(status.value)
  }

  implicit val format: Format[CustomerStatus] = Format(
    reads,
    writes
  )

}
