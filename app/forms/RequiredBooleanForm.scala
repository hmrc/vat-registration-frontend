/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}

trait RequiredBooleanForm {

  val errorMsg: String = "error.required"
  lazy val errorMsgArgs: Seq[Any] = Nil

  implicit def requiredBooleanFormatter: Formatter[Boolean] = new Formatter[Boolean] {

    override val format = Some(("format.boolean", Nil))

    // default play binding is to data.getOrElse(key, "false")
    def bind(key: String, data: Map[String, String]) = {
      Right(data.getOrElse(key,"")).right.flatMap {
        case "true" => Right(true)
        case "false" => Right(false)
        case _ => Left(Seq(FormError(key, errorMsg, errorMsgArgs)))
      }
    }

    def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  val requiredBoolean: Mapping[Boolean] = Forms.of[Boolean](requiredBooleanFormatter)

}
