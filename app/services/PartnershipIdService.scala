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

package services

import connectors.PartnershipIdConnector
import models.api.PartyType
import models.external.PartnershipIdEntity
import models.external.partnershipid.PartnershipIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import play.api.mvc.Request

@Singleton
class PartnershipIdService @Inject()(partnershipIdConnector: PartnershipIdConnector) {

  def createJourney(journeyConfig: PartnershipIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    partnershipIdConnector.createJourney(journeyConfig, partyType)
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[PartnershipIdEntity] = {
    partnershipIdConnector.getDetails(journeyId)
  }

}
