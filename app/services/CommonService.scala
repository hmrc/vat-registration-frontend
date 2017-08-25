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

import java.time.LocalDate

import cats.data.OptionT
import cats.instances.FutureInstances
import common.exceptions.DownstreamExceptions._
import connectors.KeystoreConnector
import models.ModelKeys.INCORPORATION_STATUS
import models.external.IncorporationInfo
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// TODO Refactor into a profile that's passed into services
trait CommonService extends FutureInstances {

  val keystoreConnector: KeystoreConnector = KeystoreConnector

  def fetchRegistrationId(implicit hc: HeaderCarrier): Future[String] =
    OptionT(keystoreConnector.fetchAndGet[String]("RegistrationId")).getOrElse {
      Logger.error("Could not find a registration ID in keystore")
      throw new RegistrationIdNotFoundException
    }

  def fetchDateOfIncorporation()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    OptionT(keystoreConnector.fetchAndGet[IncorporationInfo](INCORPORATION_STATUS))
      .subflatMap(_.statusEvent.incorporationDate)
      .getOrElse(throw new IllegalStateException("Date of Incorporation data expected to be found in Incorporation"))
  }

}
