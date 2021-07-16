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

import connectors.IncorpIdConnector
import models.external.IncorporatedEntity

import javax.inject.{Inject, Singleton}
import models.external.incorporatedentityid.IncorpIdJourneyConfig
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class IncorpIdService @Inject()(incorpIdConnector: IncorpIdConnector) {

  def createLimitedCompanyJourney(continueUrl: String, serviceName: String, deskProServiceId: String, signOutUrl: String)(implicit hc: HeaderCarrier): Future[String] = {
    val journeyConfig = IncorpIdJourneyConfig(continueUrl, Some(serviceName), deskProServiceId, signOutUrl)

    incorpIdConnector.createLimitedCompanyJourney(journeyConfig)
  }

  def createRegisteredSocietyJourney(continueUrl: String, serviceName: String, deskProServiceId: String, signOutUrl: String)(implicit hc: HeaderCarrier): Future[String] = {
    val journeyConfig = IncorpIdJourneyConfig(continueUrl, Some(serviceName), deskProServiceId, signOutUrl)

    incorpIdConnector.createRegisteredSocietyJourney(journeyConfig)
  }

  def getDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[IncorporatedEntity] = {
    incorpIdConnector.getDetails(journeyId)
  }

}
