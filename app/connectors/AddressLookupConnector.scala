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

import javax.inject.Inject

import config.WSHttp
import models.AddressLookupJourneyId
import models.api.ScrsAddress
import play.api.http.HeaderNames._
import play.api.http.HttpVerbs._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import uk.gov.hmrc.http.{CoreGet, CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future
import scala.util.control.NoStackTrace

class AddressLookupConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends AddressLookupConnect {
  val addressLookupFrontendUrl = config.baseUrl("address-lookup-frontend")
  val addressLookupContinueUrl = config.getConfString("address-lookup-frontend.new-address-callback.url", "")
}

trait AddressLookupConnect {
  val addressLookupFrontendUrl: String
  val addressLookupContinueUrl: String

  val http: WSHttp

  implicit val reads = ScrsAddress.adressLookupReads

  def getAddress(id: String)(implicit hc: HeaderCarrier): Future[ScrsAddress] = {
    http.GET[ScrsAddress](s"$addressLookupFrontendUrl/api/confirmed?id=$id")
  }

  def getOnRampUrl(call: Call)(implicit hc: HeaderCarrier, journeyId: AddressLookupJourneyId): Future[Call] = {
    val postUrl      = s"$addressLookupFrontendUrl/api/init/${journeyId.id}"
    val continueJson = Json.obj("continueUrl" -> s"$addressLookupContinueUrl${call.url}")

    http.POST[JsObject, HttpResponse](postUrl, continueJson).map { resp =>
      resp.header(LOCATION).map(Call(GET, _)).getOrElse { //here resp will be a 202 Accepted with a Location header
        logger.warn("[getOnRampUrl] - ERROR: Location header not set in ALF response")
        throw new ALFLocationHeaderNotSetException
      }
    }
  }
}

private[connectors] class ALFLocationHeaderNotSetException extends NoStackTrace

