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

package utils

import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

/**
  * Some of our stubs store data in session temporarily and hence require the session ID to be passed to them explicitly
  * to reconstruct the correct HeaderCarrier so the SessionIdFilter doesn't generate a new Session ID.
  *
  * The purpose of this helper is to do the above without polluting the requests we make in production with unnecessary
  * headers.
  */
object SessionIdRequestHelper {

  def conditionallyAddSessionIdHeader(baseRequest: RequestBuilder, condition: => Boolean)(implicit hc: HeaderCarrier): RequestBuilder =
    if (condition) {
      baseRequest.setHeader("X-Session-ID" -> getSessionId)
    } else {
      baseRequest
    }

  private def getSessionId(implicit hc: HeaderCarrier): String =
    hc.sessionId.getOrElse(throw new InternalServerException("No session ID in session")).value

}
