/*
 * Copyright 2018 HM Revenue & Customs
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

import common.enums.IVResult
import forms.FormValidation.{textMapping, mandatoryText}
import models.view.test.TestIVResponse
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms, Mapping}

object TestIVForm {

  implicit def ivResultFormatter: Formatter[IVResult.Value] = new Formatter[IVResult.Value] {
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case "Success"              => Right(IVResult.Success)
        case "FailedIV"             => Right(IVResult.FailedIV)
        case "UserAborted"          => Right(IVResult.UserAborted)
        case "Timeout"              => Right(IVResult.Timeout)
        case "LockedOut"            => Right(IVResult.LockedOut)
        case "InsufficientEvidence" => Right(IVResult.InsufficientEvidence)
        case _                      => Left(Seq(FormError(key, "error.required", Nil)))
      }
    }
    def unbind(key: String, value: IVResult.Value) = Map(key -> value.toString)
  }

  val ivResult: Mapping[IVResult.Value] = Forms.of[IVResult.Value](ivResultFormatter)

  val form = Form(
    mapping(
      "journeyId" -> textMapping()("journeyId").verifying(mandatoryText()("journeyId")),
      "ivResult"  -> ivResult
    )(TestIVResponse.apply)(TestIVResponse.unapply)
  )
}
