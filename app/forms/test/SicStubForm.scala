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

package forms.test

import models.test._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object SicStubForm {

  implicit val sicStubFormatter: Formatter[SicStubSelection] = new Formatter[SicStubSelection] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SicStubSelection] =
      data.get(key) match {
        case Some("SingleSicCode") => Right(SingleSicCode)
        case Some("SingleSicCodeCompliance") => Right(SingleSicCodeCompliance)
        case Some("MultipleSicCodeNoCompliance") => Right(MultipleSicCodeNoCompliance)
        case Some("MultipleSicCodeCompliance") => Right(MultipleSicCodeCompliance)
        case Some("CustomSicCodes") => Right(CustomSicCodes)
        case _ => Left(Seq(FormError(key, "Select ")))
      }

    override def unbind(key: String, value: SicStubSelection): Map[String, String] = {
      val strValue = value match {
        case SingleSicCode => "SingleSicCode"
        case SingleSicCodeCompliance => "SingleSicCodeCompliance"
        case MultipleSicCodeNoCompliance => "MultipleSicCodeNoCompliance"
        case MultipleSicCodeCompliance => "MultipleSicCodeCompliance"
        case CustomSicCodes => "CustomSicCodes"
      }

      Map(key -> strValue)
    }
  }

  val form = Form(
    mapping(
      "value" -> of[SicStubSelection],
      "sicCode1" -> optional(text),
      "sicCode2" -> optional(text),
      "sicCode3" -> optional(text),
      "sicCode4" -> optional(text)
    )(SicStub.apply)(SicStub.unapply)
  )

}

