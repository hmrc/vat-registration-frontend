/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.BusinessIdConnector
import models.api.PartyType
import models.external.BusinessIdEntity
import models.external.businessid.BusinessIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class BusinessIdService @Inject()(businessIdConnector: BusinessIdConnector) {

  def createJourney(journeyConfig: BusinessIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier): Future[String] = {
    businessIdConnector.createJourney(journeyConfig, partyType)
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[BusinessIdEntity] = {
    businessIdConnector.getDetails(journeyId)
  }

}
