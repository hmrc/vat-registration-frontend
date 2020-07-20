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

package features.bankAccountDetails.services

import javax.inject.Inject

import features.bankAccountDetails.connectors.BankAccountReputationConnector
import features.bankAccountDetails.models.BankAccountDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class BankAccountReputationServiceImpl @Inject()(val bankAccountReputationConnector: BankAccountReputationConnector) extends BankAccountReputationService

trait BankAccountReputationService {
  val bankAccountReputationConnector: BankAccountReputationConnector

  def bankAccountDetailsModulusCheck(account: BankAccountDetails)(implicit hc: HeaderCarrier): Future[Boolean] = {
    bankAccountReputationConnector.bankAccountDetailsModulusCheck(account) map {
      response => (response \ "accountNumberWithSortCodeIsValid").as[Boolean]
    }
  }
}
