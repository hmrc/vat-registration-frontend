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

import connectors.SoleTraderIdentificationConnector
import models.PersonalDetails
import models.api.PartyType
import models.external.SoleTraderIdEntity
import models.external.soletraderid.SoleTraderIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SoleTraderIdentificationService @Inject()(soleTraderIdentificationConnector: SoleTraderIdentificationConnector) {

  def startSoleTraderJourney(config: SoleTraderIdJourneyConfig,
                             partyType: PartyType)
                            (implicit hc: HeaderCarrier): Future[String] =
    soleTraderIdentificationConnector.startSoleTraderJourney(config, partyType)

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[(PersonalDetails, SoleTraderIdEntity)] =
    soleTraderIdentificationConnector.retrieveSoleTraderDetails(journeyId)

  def startIndividualJourney(config: SoleTraderIdJourneyConfig,
                             partyType: Option[PartyType] = None)
                            (implicit hc: HeaderCarrier): Future[String] =
    soleTraderIdentificationConnector.startIndividualJourney(config, partyType)

  def retrieveIndividualDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[PersonalDetails] =
    soleTraderIdentificationConnector.retrieveIndividualDetails(journeyId)
}
