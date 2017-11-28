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

package connectors.test

import javax.inject.{Inject, Singleton}

import common.enums.IVResult
import config.WSHttp
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class BusinessRegDynamicStubConnector @Inject()(config: ServicesConfig, http: WSHttp) {
  val brdsUrl = config.baseUrl("business-registration-dynamic-stub")
  val brdsUri = config.getConfString("business-registration-dynamic-stub.uri", "")

  def setupIVOutcome(journeyId: String, outcome: IVResult.Value)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.POST[String, HttpResponse](s"$brdsUrl$brdsUri/setup-iv-outcome/$journeyId/$outcome", "")
  }
}