/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject

import connectors.{IncorporationInformationConnector, KeystoreConnector}
import features.officer.services.IVService
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import utils.RegistrationWhitelist

import scala.concurrent.Future

class CurrentProfileServiceImpl @Inject()(val incorpInfoService: IncorporationInformationService,
                                          val vatRegistrationService: RegistrationService,
                                          val ivService: IVService,
                                          val keystoreConnector: KeystoreConnector) extends CurrentProfileService

trait CurrentProfileService extends RegistrationWhitelist {

  val incorpInfoService: IncorporationInformationService
  val keystoreConnector: KeystoreConnector
  val vatRegistrationService: RegistrationService
  val ivService: IVService

  def buildCurrentProfile(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    for {
      companyName           <- incorpInfoService.getCompanyName(regId, txId)
      incorpDate            <- incorpInfoService.getIncorpDate(regId, txId)
      status                <- vatRegistrationService.getStatus(regId)
      ivStatus              <-  ifRegIdNotWhitelisted(regId) {
        ivService.getIVStatus(regId)
      }(returnDefaultPassedIV)
      profile               =  CurrentProfile(
        companyName           = companyName,
        registrationId        = regId,
        transactionId         = txId,
        vatRegistrationStatus = status,
        incorporationDate     = incorpDate,
        ivPassed              = ivStatus
      )
      _                     <- incorpInfoService.registerInterest(regId, txId)
      _                     <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile)
    } yield profile
  }

  def addRejectionFlag(txId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    keystoreConnector.addRejectionFlag(txId)
  }
}
