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

package connectors

import javax.inject.Singleton

import common.enums.IVResult
import config.WSHttp
import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IdentityVerificationConnector extends ServicesConfig {
  val brdsUrl = baseUrl("business-registration-dynamic-stub")
  val brdsUri = getConfString("business-registration-dynamic-stub.uri", "")

  val http: WSHttp = WSHttp

  def getJourneyOutcome(journeyId: String)(implicit hc: HeaderCarrier): Future[IVResult.Value] =
    http.GET[JsValue](s"$brdsUrl$brdsUri/mdtp/journey/journeyId/$journeyId") map {
      _.\("result").as[IVResult.Value]
    } recover {
      case e =>
        Logger.error(s"[IdentityVerificationConnector] - [getJourneyOutcome] - There was a problem getting the IV journey outcome for journeyId $journeyId", e)
        throw e
    }
}
