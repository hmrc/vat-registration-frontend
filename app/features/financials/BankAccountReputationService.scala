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

package services {

  import javax.inject.Inject

  import connectors.BankAccountReputationConnect
  import models.view.vatFinancials.vatBankAccount.ModulusCheckAccount
  import uk.gov.hmrc.http.HeaderCarrier
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  import scala.concurrent.Future

  class BankAccountReputationService @Inject()(val bankAccountReputationConnector: BankAccountReputationConnect) extends BankAccountReputationSrv

  trait BankAccountReputationSrv {
    val bankAccountReputationConnector: BankAccountReputationConnect

    def bankDetailsModulusCheck(account: ModulusCheckAccount)(implicit hc: HeaderCarrier): Future[Boolean] = {
      bankAccountReputationConnector.bankAccountModulusCheck(account) map {
        response => (response \ "accountNumberWithSortCodeIsValid").as[Boolean]
      }
    }
  }
}

package connectors {

  import javax.inject.Inject

  import config.WSHttp
  import models.view.vatFinancials.vatBankAccount.ModulusCheckAccount
  import play.api.libs.json.JsValue
  import uk.gov.hmrc.http.{CorePost, HeaderCarrier}
  import uk.gov.hmrc.play.config.inject.ServicesConfig
  import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

  import scala.concurrent.Future

  class BankAccountReputationConnector @Inject()(val http: WSHttp, config: ServicesConfig) extends BankAccountReputationConnect{
    val bankAccountReputationUrl: String = config.baseUrl("bank-account-reputation")
  }

  trait BankAccountReputationConnect {
    val bankAccountReputationUrl: String
    val http: CorePost

    def bankAccountModulusCheck(account: ModulusCheckAccount)(implicit hc: HeaderCarrier): Future[JsValue] = {
      http.POST[ModulusCheckAccount, JsValue](s"$bankAccountReputationUrl/modcheck", account) recover {
        case ex => throw logResponse(ex, "bankAccountModulusCheck")
      }
    }
  }
}
