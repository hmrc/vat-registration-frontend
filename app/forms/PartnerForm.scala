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

package forms

import models.api.{Invalid, PartyType}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object PartnerForm {

  val leadPartnerEntityType: String = "value"

  def formatter(implicit error: String): Formatter[PartyType] = new Formatter[PartyType] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], PartyType] = {
      data.get(key).map(value => PartyType.inverseStati(value)) match {
        case Some(Invalid) => Left(Seq(FormError(key, error)))
        case Some(value) => Right(value)
        case _ => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, value: PartyType): Map[String, String] =
      Map(key -> PartyType.stati(value))
  }

  def form(implicit error: String): Form[PartyType] = Form(
    single(
      leadPartnerEntityType -> of(formatter)
    )
  )

}
