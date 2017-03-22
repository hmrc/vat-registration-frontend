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

package services

import java.time.LocalDate
import javax.inject.Inject

import com.google.inject.ImplementedBy
import connectors.BankHolidaysConnector
import play.api.Logger
import play.api.cache.CacheApi
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Await
import scala.util.Try

@ImplementedBy(classOf[WorkingDaysService])
trait DateService {

  def addWorkingDays(date: LocalDate, days: Int): LocalDate

}

class WorkingDaysService @Inject()(bankHolidaysConnector: BankHolidaysConnector, cacheApi: CacheApi)
  extends DateService {

  import services.WorkingDaysService.BANK_HOLIDAYS_CACHE_KEY
  import uk.gov.hmrc.time.workingdays._

  import scala.concurrent.duration._
  import scala.language.postfixOps

  val defaultHolidaySet = Await.result(bankHolidaysConnector.bankHolidays()(HeaderCarrier()), 5 seconds)

  //TODO use scalamock, as Mockito can't mock CacheApi#getOrElse : call-by-name parameters
  // $COVERAGE-OFF$

  override def addWorkingDays(date: LocalDate, days: Int): LocalDate = {
    import scala.concurrent.duration._

    implicit val hols: BankHolidaySet = cacheApi.getOrElse(BANK_HOLIDAYS_CACHE_KEY, 1 day) {
      Logger.info(s"Reloading cache entry for $BANK_HOLIDAYS_CACHE_KEY")
      Try {
        Await.result(bankHolidaysConnector.bankHolidays()(HeaderCarrier()), 5 seconds)
      }.getOrElse(defaultHolidaySet)
    }

    import common.DateConversions._
    val jodaDate: org.joda.time.LocalDate = date
    jodaDate.plusWorkingDays(days)
  }

  // $COVERAGE-ON$

}

// $COVERAGE-OFF$
object WorkingDaysService {
  val BANK_HOLIDAYS_CACHE_KEY = "bankHolidaySet"
}

// $COVERAGE-ON$
//
