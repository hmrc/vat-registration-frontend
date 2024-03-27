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

import play.api.http.Status
import uk.gov.hmrc.http.{BadRequestException, NotFoundException, Upstream4xxResponse, Upstream5xxResponse}
import utils.LoggingUtil
import play.api.mvc.Request

package object connectors extends LoggingUtil {
  def logResponse(e: Throwable, func: String)(implicit request: Request[_]): Throwable = {
    e match {
      case e: NotFoundException   =>
        warnLog(s"[$func] received NOT FOUND")
      case e: BadRequestException =>
        warnLog(s"[$func] received BAD REQUEST")
      case e: Upstream4xxResponse => e.upstreamResponseCode match {
        case Status.FORBIDDEN =>
          errorLog(s"[$func] received FORBIDDEN")
        case _ =>
          errorLog(s"[$func] received Upstream 4xx: ${e.upstreamResponseCode}")
      }
      case e: Upstream5xxResponse =>
        errorLog(s"[$func] received Upstream 5xx: ${e.upstreamResponseCode}")
      case e: Exception           =>
        errorLog(s"[$func] received unexpected error")
    }
    e
  }
}
