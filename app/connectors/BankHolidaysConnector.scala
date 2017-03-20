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

package connectors

import javax.inject.{Inject, Singleton}

import config.WSHttp
import play.api.libs.json.Json
import play.api.{Environment, Mode}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class BankHolidaysConnector @Inject()(environment: Environment) {

  lazy val uri = if (environment.mode == Mode.Test) "http://localhost:11111/bank-holidays.json" else "https://www.gov.uk/bank-holidays.json"

  implicit val bankHolidayReads = Json.reads[BankHoliday]
  implicit val bankHolidaySetReads = Json.reads[BankHolidaySet]

  def bankHolidays(division: String = "england-and-wales")(implicit headerCarrier: HeaderCarrier): Future[BankHolidaySet] =
    WSHttp.GET[Map[String, BankHolidaySet]](uri) map { holidaySets => holidaySets(division) }

}
