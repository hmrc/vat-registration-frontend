/*
 * Copyright 2017 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}



import common.enums.VatRegStatus
import connectors.KeystoreConnector
import models.CurrentProfile
import models.external.CoHoCompanyProfile
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CurrentProfileService @Inject()(val incorpInfoService: IncorpInfoService) extends CurrentProfileSrv {
  val keystoreConnector = KeystoreConnector
}

trait CurrentProfileSrv {

  val incorpInfoService: IncorpInfoService
  val keystoreConnector: KeystoreConnector

  def buildCurrentProfile(regId: String, txId: String)(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    for {
      companyName           <- incorpInfoService.getCompanyName(regId, txId)
      incorpInfo            <- incorpInfoService.getIncorporationInfo(txId).value
      incorpDate            =  if(incorpInfo.isDefined) incorpInfo.get.statusEvent.incorporationDate else None
      profile               =  CurrentProfile(
        companyName           = companyName,
        registrationId        = regId,
        transactionId         = txId,
        vatRegistrationStatus = VatRegStatus.DRAFT,
        incorporationDate     = incorpDate
      )
      _                     <- keystoreConnector.cache[CurrentProfile]("CurrentProfile", profile)
    } yield profile
  }
}

