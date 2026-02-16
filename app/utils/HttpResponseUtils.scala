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

package utils

import play.api.libs.json.{JsError, Reads}
import uk.gov.hmrc.http.HttpResponse

import scala.util.{Failure, Success, Try}

object HttpResponseUtils {

  implicit class RichHttpResponse(val response: HttpResponse) extends AnyVal {

    def parseJSON[A](implicit reads: Reads[A]): Either[String, A] =
      Try(response.json) match {
        case Success(jsValue) =>
          jsValue.validate[A].fold(
            errors =>
              Left(s"Could not parse http response JSON: ${JsError.toJson(errors)}"),
            value =>
              Right(value)
          )

        case Failure(e) =>
          Left(s"Could not read http response as JSON: ${Option(e.getMessage).getOrElse(e.toString)}")
      }
  }
}