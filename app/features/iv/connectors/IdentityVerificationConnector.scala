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

import javax.inject.{Inject, Singleton}

import common.enums.IVResult
import config.WSHttp
import features.iv.models.IVSetup
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp
import utils.VATRegFeatureSwitch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class IdentityVerificationConnector @Inject()(vatRegFeatureSwitch: VATRegFeatureSwitch) extends ServicesConfig with ivConnect {
  val brdsUrl = baseUrl("business-registration-dynamic-stub")
  val brdsUri = getConfString("business-registration-dynamic-stub.uri", throw new Exception)
  val ivProxyUrl: String = baseUrl("iv.identity-verification-proxy")
  val ivProxyUri: String = getConfString("iv.identity-verification-proxy.uri", throw new Exception)
  val ivFeUrl = getConfString("iv.identity-verification-frontend.www.url", throw new Exception)
  val ivBase =  baseUrl("iv.identity-verification-frontend")

  def useIvStub = vatRegFeatureSwitch.useIvStub.enabled

  val http: WSHttp = WSHttp

  def getJourneyOutcome(journeyId: String)(implicit hc: HeaderCarrier): Future[IVResult.Value] = {
    val url = if (useIvStub) (brdsUrl + brdsUri) else ivBase
    http.GET[JsValue](s"${url}/mdtp/journey/journeyId/${journeyId}") map {
      _.\("result").as[IVResult.Value]
    } recover {
      case e =>
        Logger.error(s"[IdentityVerificationConnector] - [getJourneyOutcome] - There was a problem getting the IV journey outcome for journeyId $journeyId", e)
        throw e
    }
  }

  def setupIVJourney(ivSetupData: IVSetup)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST[IVSetup, JsValue](s"$ivProxyUrl$ivProxyUri/journey/start", ivSetupData).map{ a =>
      val link = a.as[JsObject] \ "link"
      a.as[JsObject] - "link" ++ JsObject(Map("link" -> link.toOption.map(b => Json.toJson(ivFeUrl + b.as[String])).head))
    }.recover{
      case e =>
        Logger.error(s"[IdentityVerificationConnector] - [setupIVJourney] - There was a problem Setting up the IV journey", e)
        throw e
    }
  }
}
trait ivConnect{
  val brdsUrl:String
  val brdsUri:String
  val ivProxyUrl:String
  val ivProxyUri:String
  val ivFeUrl:String
}

