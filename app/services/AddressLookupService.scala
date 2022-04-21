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

package services

import common.enums.AddressLookupJourneyIdentifier
import config.AddressLookupConfiguration
import connectors.AddressLookupConnector
import javax.inject.{Inject, Singleton}
import models.api.Address
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class AddressLookupService @Inject()(val addressLookupConnector: AddressLookupConnector,
                                     alfConfig: AddressLookupConfiguration) {

  def getAddressById(id: String)(implicit hc: HeaderCarrier): Future[Address] = addressLookupConnector.getAddress(id)

  def getJourneyUrl(journeyId: AddressLookupJourneyIdentifier.Value, continueUrl: Call, useUkMode: Boolean = false)(implicit hc: HeaderCarrier): Future[Call] = {
    addressLookupConnector.getOnRampUrl(alfConfig(journeyId, continueUrl, useUkMode))
  }

}
