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

import java.io.InputStream
import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.{Environment, Logger}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BankHolidaysConnector {
  implicit val bankHolidayReads = Json.reads[BankHoliday]
  implicit val bankHolidaySetReads = Json.reads[BankHolidaySet]

  def bankHolidays(division: String = "england-and-wales")(implicit headerCarrier: HeaderCarrier): Future[BankHolidaySet]

  protected def filterByDivision(holidays: Future[Map[String, BankHolidaySet]], division: String): Future[BankHolidaySet] =
    holidays map { holidaySets => holidaySets(division) }

}

@Singleton
class WSBankHolidaysConnector @Inject()(wsHttp: WSHttp, configuration: ServicesConfig) extends BankHolidaysConnector {

  lazy val url = configuration.getConfString("bank-holidays.url", "")

  def bankHolidays(division: String = "england-and-wales")(implicit headerCarrier: HeaderCarrier): Future[BankHolidaySet] =
    filterByDivision(wsHttp.GET[Map[String, BankHolidaySet]](url), division)

}


class FallbackBankHolidaysConnector @Inject()(environment: Environment) extends BankHolidaysConnector {

  override def bankHolidays(division: String = "england-and-wales")(implicit headerCarrier: HeaderCarrier): Future[BankHolidaySet] = {
    Logger.info("Loading static set of bank holidays from classpath file: bank-holidays.json")
    val resourceAsStream: InputStream = environment.classLoader.getResourceAsStream("bank-holidays.json")
    //if below .get fails, app startup fails. This is as expected. bank-holidays.json file must be on classpath
    val parsed = Json.parse(resourceAsStream).asOpt[Map[String, BankHolidaySet]].get
    filterByDivision(Future.successful(parsed), division)
  }

}