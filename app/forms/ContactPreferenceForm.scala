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

package forms

import models.{ContactPreference, Email, Letter}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object ContactPreferenceForm {

  val contactPreference: String = "value"

  val email: String = "email"

  val letter: String = "letter"

  val contactPreferenceError: String = "contactPreference.error.required"


  def apply(): Form[ContactPreference] = Form(
    single(
      contactPreference -> of(formatter)
    )
  )

  def formatter: Formatter[ContactPreference] = new Formatter[ContactPreference] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ContactPreference] = {
      data.get(key) match {
        case Some(`email`) => Right(Email)
        case Some(`letter`) => Right(Letter)
        case _ => Left(Seq(FormError(key, contactPreferenceError)))
      }
    }

    override def unbind(key: String, value: ContactPreference): Map[String, String] = {
      val stringValue = value match {
        case Email => email
        case Letter => letter
      }
      Map(key -> stringValue)
    }
  }

  def contactPreferenceForm: Form[ContactPreference] = Form(
    single(
      contactPreference -> of(formatter)
    )
  )


}
