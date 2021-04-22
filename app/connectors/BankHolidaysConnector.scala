/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.JodaReads.jodaLocalDateReads
import play.api.libs.json.{Json, Reads}
import play.api.Environment
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import java.io.InputStream
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BankHolidaysConnector @Inject()(http: HttpClient, config: FrontendAppConfig)
                                     (implicit ec: ExecutionContext) {

  implicit val jodaReads = jodaLocalDateReads("yyyy-MM-dd")
  protected implicit val bankHolidayReads: Reads[BankHoliday] = Json.reads[BankHoliday]
  protected implicit val bankHolidaySetReads: Reads[BankHolidaySet] = Json.reads[BankHolidaySet]

  lazy val url: String = config.bankHolidaysUrl

  def bankHolidays(division: String = "england-and-wales")(implicit hc: HeaderCarrier): Future[BankHolidaySet] = {
    http.GET[Map[String, BankHolidaySet]](url) map {
      holidaySets => holidaySets(division)
    }
  }
}

@Singleton
class FallbackBankHolidaysConnector @Inject()(environment: Environment) {

  implicit val jodaReads = jodaLocalDateReads("yyyy-MM-dd")
  protected implicit val bankHolidayReads: Reads[BankHoliday] = Json.reads[BankHoliday]
  protected implicit val bankHolidaySetReads: Reads[BankHolidaySet] = Json.reads[BankHolidaySet]

  def bankHolidays(division: String = "england-and-wales"): Future[BankHolidaySet] = {
    logger.info("Loading static set of bank holidays from classpath file: bank-holidays.json")
    val resourceAsStream: InputStream = environment.classLoader.getResourceAsStream("bank-holidays.json")
    //if below .get fails, app startup fails. This is as expected. bank-holidays.json file must be on classpath
    val parsed = Json.parse(resourceAsStream).asOpt[Map[String, BankHolidaySet]].get
    Future.successful(parsed(division))
  }
}
