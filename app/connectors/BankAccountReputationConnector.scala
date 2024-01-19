/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, StringContextOps}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import utils.LoggingUtil

@Singleton
class BankAccountReputationConnector @Inject()(val http: HttpClientV2,
                                               appConfig: FrontendAppConfig)
                                              (implicit ec: ExecutionContext) extends LoggingUtil {

  def validateBankDetails(account: BankAccountDetails)(implicit hc: HeaderCarrier, request: Request[_]): Future[JsValue] = {
    http.post(url"${appConfig.validateBankDetailsUrl}")
      .withBody(Json.toJson(account))
      .execute
      .map { response =>
        response.status match {
          case OK =>
            infoLog("Bank account details validated successfully")
            response.json
          case status =>
            val errorMessage = s"Unexpected status returned by Bank Account Reputation: $status"
            errorLog(errorMessage)
            throw new InternalServerException(errorMessage)
        }
      }.recover {
        case ex =>
          val errorMessage = s"Something went wrong when calling bank account validation API: ${ex.getMessage}"
          errorLog(errorMessage)
          throw new InternalServerException(errorMessage)
      }
  }
}

