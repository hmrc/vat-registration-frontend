/*
 * Copyright 2026 HM Revenue & Customs
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
import models.api.bars.request.{BarsVerifyBusinessRequest, BarsVerifyPersonalRequest}
import models.api.bars.response.BarsVerifyResponse
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BarsConnector @Inject()(httpClient: HttpClientV2, config: FrontendAppConfig)
                             (implicit ec: ExecutionContext) {

  /** "This endpoint checks the likely correctness of a given personal bank account and it's likely connection to the
   * given account holder (aka the subject)"
   */
  private val verifyPersonalUrl: String = s"${config.verifyBarsPersonalUrl}"

  def verifyPersonal(barsVerifyPersonalRequest: BarsVerifyPersonalRequest)(
    implicit hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .post(url"$verifyPersonalUrl")
      .withBody(Json.toJson(barsVerifyPersonalRequest))
      .execute[HttpResponse]
  }

  /** "This endpoint checks the likely correctness of a given business bank account and it's likely connection to the
   * given business"
   */
  private val verifyBusinessUrl: String = s"${config.verifyBarsBusinessUrl}"

  def verifyBusiness(barsVerifyBusinessRequest: BarsVerifyBusinessRequest)(implicit hc: HeaderCarrier
  ): Future[HttpResponse] = {
    httpClient
      .post(url"$verifyBusinessUrl")
      .withBody(Json.toJson(barsVerifyBusinessRequest))
      .execute[HttpResponse]
  }
}