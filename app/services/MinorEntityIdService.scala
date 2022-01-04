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

import connectors.MinorEntityIdConnector
import models.api.PartyType
import models.external.MinorEntity
import models.external.minorentityid.MinorEntityIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class MinorEntityIdService @Inject()(minorEntityIdConnector: MinorEntityIdConnector) {

  def createJourney(journeyConfig: MinorEntityIdJourneyConfig, partyType: PartyType)(implicit hc: HeaderCarrier): Future[String] = {
    minorEntityIdConnector.createJourney(journeyConfig, partyType)
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[MinorEntity] = {
    minorEntityIdConnector.getDetails(journeyId)
  }

}
