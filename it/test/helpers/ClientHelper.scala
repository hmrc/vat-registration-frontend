/*
 * Copyright 2017 HM Revenue & Customs
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

package helpers

import play.api.http.HeaderNames
import play.api.libs.ws.{WSRequest, WSResponse}
import support.AppAndStubs

trait ClientHelper extends AuthHelper {
  this: AppAndStubs =>

  def redirectLocation(response: WSResponse): Option[String] = response.header("Location")

  implicit class RichClient(req: WSRequest) {
    def withSessionCookieHeader(userId: String = defaultUser): WSRequest =
      req.withHttpHeaders(HeaderNames.COOKIE -> getSessionCookie(userId = userId))

    def withCSRFTokenHeader: WSRequest = req.withHttpHeaders("Csrf-Token" -> "nocheck")
  }
}
