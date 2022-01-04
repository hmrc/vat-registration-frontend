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

package models

import org.scalatest.Assertion
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

trait JsonFormatValidation extends PlaySpec {

  implicit class JsResultOps[T](res: JsResult[T]) {

    def resultsIn(t: T): Assertion = res match {
      case JsSuccess(deserialisedT, _) => deserialisedT mustBe t
      case JsError(errors) => fail(s"found errors: $errors when expected: $t")
    }

    def shouldHaveErrors(expectedErrors: (JsPath, JsonValidationError)*): Unit = {
      val errorMap = Map(expectedErrors: _*)
      res match {
        case JsSuccess(t, _) => fail(s"read should have failed and didn't - produced $t")
        case JsError(errors) =>
          errors.size mustBe errorMap.size
          for ((path, validationErrors) <- errors) {
            errorMap.keySet must contain(path)
            validationErrors must contain(errorMap(path))
          }
      }
    }
  }

}
