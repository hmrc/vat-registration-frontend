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

import auth.VatExternalUrls.getConfString
import com.google.inject.ImplementedBy
import config.WSHttp
import models.AddressLookupJourneyId
import models.api.ScrsAddress
import play.api.Logger
import play.api.http.HeaderNames._
import play.api.http.HttpVerbs._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NoStackTrace

@Singleton
class AddressLookupConnector @Inject()() extends AddressLookupConnect with ServicesConfig {

  val http: WSHttp = WSHttp
  val addressLookupFrontendUrl = baseUrl("address-lookup-frontend")
  val addressLookupContinueUrl = getConfString("address-lookup-frontend.new-address-callback.url", "")

  implicit val reads = ScrsAddress.adressLookupReads

  def getAddress(id: String)(implicit hc: HeaderCarrier): Future[ScrsAddress] =
    http.GET[ScrsAddress](s"$addressLookupFrontendUrl/api/confirmed?id=$id")

  def getOnRampUrl(call: Call)(implicit hc: HeaderCarrier, journeyId: AddressLookupJourneyId): Future[Call] = {
    val postUrl = s"$addressLookupFrontendUrl/api/init/${journeyId.id}"
    val continueJson = Json.obj("continueUrl" -> s"$addressLookupContinueUrl${call.url}")

    http.POST[JsObject, HttpResponse](postUrl, continueJson) map { resp =>
      Logger.debug(s"Response from ALF: $resp")
      resp.header(LOCATION).map(Call(GET, _)).getOrElse {
        Logger.warn("[AddressLookupConnector] [getOnRampUrl] - ERROR: Location header not set in ALF response")
        throw new ALFLocationHeaderNotSetException
      }
    }
  }
}

class ALFLocationHeaderNotSetException extends NoStackTrace


@ImplementedBy(classOf[AddressLookupConnector])
trait AddressLookupConnect {

  val addressLookupFrontendUrl: String
  val addressLookupContinueUrl: String
  val http: WSHttp

  def getAddress(id: String)(implicit hc: HeaderCarrier): Future[ScrsAddress]

  def getOnRampUrl(call: Call)(implicit hc: HeaderCarrier, journeyId: AddressLookupJourneyId): Future[Call]

}

