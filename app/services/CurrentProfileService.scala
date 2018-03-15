/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.KeystoreConnect
import features.officer.services.IVService
import models.CurrentProfile
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class CurrentProfileService @Inject()(val incorpInfoService: IncorporationInformationService,
                                      val vatRegistrationService: RegistrationService,
                                      val ivService: IVService,
                                      val keystoreConnector: KeystoreConnect) extends CurrentProfileSrv

trait CurrentProfileSrv {

  val incorpInfoService: IncorporationInformationService
  val keystoreConnector: KeystoreConnect
  val vatRegistrationService: RegistrationService
  val ivService: IVService

  def buildCurrentProfile(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    for {
      companyName           <- incorpInfoService.getCompanyName(regId, txId)
      incorpInfo            <- incorpInfoService.getIncorporationInfo(txId)
      status                <- vatRegistrationService.getStatus(regId)
      ivStatus              <- ivService.getIVStatus(regId)
      incorpDate            =  if(incorpInfo.isDefined) incorpInfo.get.statusEvent.incorporationDate else None
      profile               =  CurrentProfile(
        companyName           = companyName,
        registrationId        = regId,
        transactionId         = txId,
        vatRegistrationStatus = status,
        incorporationDate     = incorpDate,
        ivPassed              = ivStatus
      )
      _                     <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile)
    } yield profile
  }
}
