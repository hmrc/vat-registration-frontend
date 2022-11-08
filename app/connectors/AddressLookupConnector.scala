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

package connectors

import config.FrontendAppConfig
import models.api.Address
import models.external.addresslookup.AddressLookupConfigurationModel
import play.api.http.HeaderNames._
import play.api.http.HttpVerbs._
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class AddressLookupConnector @Inject()(val http: HttpClientV2, appConfig: FrontendAppConfig)
                                      (implicit ec: ExecutionContext) {

  implicit val reads: Address.addressLookupReads.type = Address.addressLookupReads

  def getAddress(id: String)(implicit hc: HeaderCarrier): Future[Address] =
    http.get(url"${appConfig.addressLookupRetrievalUrl(id)}")
      .execute[Address]

  def getOnRampUrl(alfConfig: AddressLookupConfigurationModel)(implicit hc: HeaderCarrier): Future[Call] =
    http.post(url"${appConfig.addressLookupJourneyUrl}")
      .withBody(Json.toJson(alfConfig))
      .execute
      .map { resp =>
        resp.header(LOCATION).map(Call(GET, _)).getOrElse { //here resp will be a 202 Accepted with a Location header
          logger.warn("[getOnRampUrl] - ERROR: Location header not set in ALF response")
          throw new ALFLocationHeaderNotSetException
        }
      }
}

private[connectors] class ALFLocationHeaderNotSetException extends NoStackTrace
