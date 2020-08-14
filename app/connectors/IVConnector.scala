/*
 * Copyright 2020 HM Revenue & Customs
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

import config.WSHttp
import javax.inject.{Inject, Singleton}
import models.{IVResult, IVSetup}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.VATRegFeatureSwitches

import scala.concurrent.Future

@Singleton
class IVConnector @Inject()(vatRegFeatureSwitch: VATRegFeatureSwitches,
                            config: ServicesConfig,
                            val http: WSHttp) {
  val brdsUrl: String = config.baseUrl("business-registration-dynamic-stub")
  val brdsUri: String = config.getConfString("business-registration-dynamic-stub.uri", throw new Exception)
  val ivProxyUrl: String = config.baseUrl("iv.identity-verification-proxy")
  val ivProxyUri: String = config.getConfString("iv.identity-verification-proxy.uri", throw new Exception)
  val ivFeUrl: String = config.getConfString("iv.identity-verification-frontend.www.url", throw new Exception)
  val ivBase: String = config.baseUrl("iv.identity-verification-frontend")

  def useIvStub: Boolean = vatRegFeatureSwitch.useIvStub.enabled

  def getJourneyOutcome(journeyId: String)(implicit hc: HeaderCarrier): Future[IVResult.Value] = {
    val url = if (useIvStub) brdsUrl + brdsUri else ivBase
    http.GET[JsValue](s"$url/mdtp/journey/journeyId/$journeyId") map {
      _.\("result").as[IVResult.Value]
    } recover {
      case e =>
        Logger.error(s"[IdentityVerificationConnector] - [getJourneyOutcome] - There was a problem getting the IV journey outcome for journeyId $journeyId", e)
        throw e
    }
  }

  def setupIVJourney(ivSetupData: IVSetup)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST[IVSetup, JsValue](s"$ivProxyUrl$ivProxyUri/journey/start", ivSetupData).map { a =>
      val link = a.as[JsObject] \ "link"
      a.as[JsObject] - "link" ++ JsObject(Map("link" -> link.toOption.map(b => Json.toJson(ivFeUrl + b.as[String])).head))
    }.recover {
      case e =>
        Logger.error(s"[IdentityVerificationConnector] - [setupIVJourney] - There was a problem Setting up the IV journey", e)
        throw e
    }
  }
}
