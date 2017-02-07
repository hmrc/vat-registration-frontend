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

import connectors.KeystoreConnector
import common.exceptions.DownstreamExceptions._
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait CommonService {

  val keystoreConnector: KeystoreConnector

  def fetchRegistrationId(implicit hc: HeaderCarrier): Future[String] = {
    keystoreConnector.fetchAndGet[String]("RegistrationId").map {
      case Some(regId) => regId
      case None =>
        Logger.error("Could not find a registration ID in keystore")
        throw new RegistrationIdNotFoundException
    }
  }

}
