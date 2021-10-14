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

import connectors.SoleTraderIdentificationConnector
import models.TransactorDetails
import models.api.PartyType
import models.external.SoleTraderIdEntity
import models.external.soletraderid.SoleTraderIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderIdentificationService @Inject()(soleTraderIdentificationConnector: SoleTraderIdentificationConnector)
                                               (implicit ec: ExecutionContext) {

  def startJourney(continueUrl: String,
                   serviceName: String,
                   deskproId: String,
                   signOutUrl: String,
                   accessibilityUrl: String,
                   enableSautrCheck: Boolean,
                   partyType: PartyType)
                  (implicit hc: HeaderCarrier): Future[String] =
    soleTraderIdentificationConnector.startJourney(config = SoleTraderIdJourneyConfig(
      continueUrl = continueUrl,
      optServiceName = Some(serviceName),
      deskProServiceId = deskproId,
      signOutUrl = signOutUrl,
      accessibilityUrl = accessibilityUrl,
      enableSautrCheck = enableSautrCheck
    ), partyType)

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[(TransactorDetails, SoleTraderIdEntity)] =
    soleTraderIdentificationConnector.retrieveSoleTraderDetails(journeyId)
}
