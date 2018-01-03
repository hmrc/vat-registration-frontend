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

import common.enums.RegistrationDeletion
import connectors.{CompanyRegistrationConnect, KeystoreConnect, RegistrationConnector, S4LConnect}
import models.CurrentProfile
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class CancellationServiceImpl @Inject()(val keystoreConnector: KeystoreConnect,
                                        val currentProfileService: CurrentProfileSrv,
                                        val companyRegistrationConnector: CompanyRegistrationConnect,
                                        val save4LaterConnector: S4LConnect,
                                        val vatRegistrationConnector: RegistrationConnector) extends CancellationService

trait CancellationService {
  val keystoreConnector: KeystoreConnect
  val currentProfileService: CurrentProfileSrv
  val companyRegistrationConnector: CompanyRegistrationConnect
  val save4LaterConnector: S4LConnect
  val vatRegistrationConnector: RegistrationConnector

  private val CURRENT_PROFILE_KEY = "CurrentProfile"

  private val logger: Logger = LoggerFactory.getLogger(getClass)

  def deleteVatRegistration(regId: String)(implicit hc: HeaderCarrier): Future[RegistrationDeletion.Value] = {
    getCurrentProfile(regId) flatMap { profile =>
      if(profile.registrationId == regId) {
        for {
          _ <- vatRegistrationConnector.deleteVREFESession(regId)
          _ <- save4LaterConnector.clear(regId)
          _ <- keystoreConnector.remove
          _ <- vatRegistrationConnector.deleteVatScheme(regId)
        } yield {
          logger.warn(s"[deleteVatRegistration] - deleted vat scheme for $regId")
          RegistrationDeletion.deleted
        }
      } else {
        Future.successful(RegistrationDeletion.forbidden)
      }
    }
  }

  private def getCurrentProfile(regId: String)(implicit hc: HeaderCarrier): Future[CurrentProfile] = {
    keystoreConnector.fetchAndGet[CurrentProfile](CURRENT_PROFILE_KEY) flatMap {
      _.fold(buildNewProfile(regId))(profile => Future.successful(profile))
    }
  }

  private def buildNewProfile(regId: String)(implicit hc: HeaderCarrier): Future[CurrentProfile] = for {
    txId    <- companyRegistrationConnector.getTransactionId(regId)
    profile <- currentProfileService.buildCurrentProfile(regId, txId)
  } yield profile
}
