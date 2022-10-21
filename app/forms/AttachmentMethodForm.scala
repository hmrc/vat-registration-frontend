/*
 * Copyright 2022 HM Revenue & Customs
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

import models.api._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

class AttachmentMethodForm {

  def apply(): Form[AttachmentMethod] = Form[AttachmentMethod](
    single(
      AttachmentMethodForm.fieldName -> of(AttachmentMethodForm.formatter))
  )

}

object AttachmentMethodForm {

  val fieldName = "value"

  private object Messages {
    val invalidSelection = "attachmentMethod.error.missing"
  }

  private object Options {
    val Upload = "2"
    val Post = "3"
    val Email = "email"
  }

  def formatter: Formatter[AttachmentMethod] = new Formatter[AttachmentMethod] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AttachmentMethod] = {
      data.get(key) match {
        case Some(Options.Upload) => Right(Attached)
        case Some(Options.Post) => Right(Post)
        case Some(Options.Email) => Right(EmailMethod)
        case _ => Left(Seq(FormError(key, Messages.invalidSelection)))
      }
    }

    override def unbind(key: String, value: AttachmentMethod): Map[String, String] = {
      val selectedOption = value match {
        case Attached => Options.Upload
        case Post => Options.Post
        case EmailMethod => Options.Email
      }
      Map(key -> selectedOption)
    }
  }

}
