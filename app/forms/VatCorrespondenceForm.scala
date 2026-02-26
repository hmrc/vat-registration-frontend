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

package forms

import models.{English, Language, Welsh}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object VatCorrespondenceForm {

  val vatCorrespondence: String = "value"

  val english: String = "english"

  val welsh: String = "welsh"

  val vatCorrespondenceError: String = "vatCorrespondence.error.required"


  def apply(): Form[Language] = Form(
    single(
      vatCorrespondence -> of(formatter)
    )
  )

  def formatter: Formatter[Language] = new Formatter[Language] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Language] = {
      data.get(key) match {
        case Some(`english`) => Right(English)
        case Some(`welsh`) => Right(Welsh)
        case _ => Left(Seq(FormError(key, vatCorrespondenceError)))
      }
    }

    override def unbind(key: String, value: Language): Map[String, String] = {
      val stringValue = value match {
        case English => english
        case Welsh => welsh
      }
      Map(key -> stringValue)
    }
  }

}
