/*
 * Copyright 2022 HM Revenue & Customs
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


import config.FrontendAppConfig
import models.BankAccountDetails
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, InternalServerException}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import play.api.http.Status.OK

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BankAccountReputationConnector @Inject()(val http: HttpClient,
                                               appConfig: FrontendAppConfig)
                                              (implicit ec: ExecutionContext) {


  def validateBankDetails(account: BankAccountDetails)(implicit hc: HeaderCarrier): Future[JsValue] = {
    http.POST[BankAccountDetails, HttpResponse](appConfig.validateBankDetailsUrl, account)
      .map { response =>
        response.status match {
          case OK => response.json
          case status => throw new InternalServerException(s"Unexpected status returned by Bank Account Reputation: $status")
        }
      }.recover {
        case ex => throw new InternalServerException(s"Something went wrong when calling bank account validation API: ${ex.getMessage}")
      }
  }
}
