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

package services {

  import javax.inject.{Inject, Singleton}

  import connectors.BankAccountReputationConnector
  import models.view.vatFinancials.vatBankAccount.ModulusCheckAccount
  import uk.gov.hmrc.play.http.HeaderCarrier

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  @Singleton
  class BankAccountReputationService @Inject()(val bankAccountReputationConnector: BankAccountReputationConnector) extends BankAccountReputationSrv

  trait BankAccountReputationSrv {

    val bankAccountReputationConnector: BankAccountReputationConnector

    def bankDetailsModulusCheck(account: ModulusCheckAccount)(implicit hc: HeaderCarrier): Future[Boolean] = {
      bankAccountReputationConnector.bankAccountModulusCheck(account) map {
        response => (response \ "accountNumberWithSortCodeIsValid").as[Boolean]
      }
    }
  }
}

package connectors {

  import javax.inject.Singleton

  import config.WSHttp
  import models.view.vatFinancials.vatBankAccount.ModulusCheckAccount
  import play.api.Logger
  import play.api.libs.json.JsValue
  import uk.gov.hmrc.play.config.ServicesConfig
  import uk.gov.hmrc.play.http.{HeaderCarrier, Upstream5xxResponse}
  import uk.gov.hmrc.play.http.ws.WSHttp

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future

  @Singleton
  class BankAccountReputationConnector extends BankAccountReputationConnect with ServicesConfig {
    val bankAccountReputationUrl: String = baseUrl("bank-account-reputation")
    val http: WSHttp = WSHttp
  }

  trait BankAccountReputationConnect {

    val bankAccountReputationUrl: String
    val http: WSHttp

    def bankAccountModulusCheck(account: ModulusCheckAccount)(implicit hc: HeaderCarrier): Future[JsValue] = {
      http.POST[ModulusCheckAccount, JsValue](s"$bankAccountReputationUrl/modcheck", account) recover {
        case ex => logResponse(ex, "BankAccountReputationConnector","bankAccountModulusCheck")
          throw ex
      }
    }
  }

}
