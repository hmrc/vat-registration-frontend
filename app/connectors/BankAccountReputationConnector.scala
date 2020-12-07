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

package connectors


import javax.inject.{Inject, Singleton}
import models.BankAccountDetails
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountReputationConnector @Inject()(val http: HttpClient, config: ServicesConfig)
                                              (implicit ec: ExecutionContext) {

  val bankAccountReputationUrl: String = config.baseUrl("bank-account-reputation")

  def bankAccountDetailsModulusCheck(account: BankAccountDetails)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST[BankAccountDetails, JsValue](s"$bankAccountReputationUrl/modcheck", account) recover {
      case ex => throw logResponse(ex, "bankAccountModulusCheck")
    }
  }
}
