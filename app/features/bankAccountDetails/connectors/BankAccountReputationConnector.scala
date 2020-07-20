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

package features.bankAccountDetails.connectors

import javax.inject.Inject

import config.WSHttp
import connectors.logResponse
import features.bankAccountDetails.models.BankAccountDetails
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{CorePost, HeaderCarrier}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class BankAccountReputationConnectorImpl @Inject()(val http: WSHttp, config: ServicesConfig) extends BankAccountReputationConnector{
  val bankAccountReputationUrl: String = config.baseUrl("bank-account-reputation")
}

trait BankAccountReputationConnector {
  val bankAccountReputationUrl: String
  val http: CorePost

  def bankAccountDetailsModulusCheck(account: BankAccountDetails)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST[BankAccountDetails, JsValue](s"$bankAccountReputationUrl/modcheck", account) recover {
      case ex => throw logResponse(ex, "bankAccountModulusCheck")
    }
  }
}
