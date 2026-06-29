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

package services

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DateTimeUtils._
import utils.SystemDate
import utils.workingdays.WorkingDays._
import utils.workingdays._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class TimeService @Inject()(val bankHolidaysService: BankHolidaysService,
                            val servicesConfig: ServicesConfig) {


  lazy val dayEndHour: Int = servicesConfig.getInt("time-service.day-end-hour")
  val DATE_FORMAT = "yyyy-MM-dd"

  def today: LocalDate = LocalDate.now()

  def isDateSomeWorkingDaysInFuture(futureDate: LocalDate): Boolean = {
    isEqualOrAfter(getMinWorkingDayInFuture, futureDate)
  }

  def getMinWorkingDayInFuture: LocalDate = {
    addWorkingDays(currentLocalDate, getDaysInAdvance(currentDateTime.getHour) + 1)
  }

  def addWorkingDays(date: LocalDate, days: Int): LocalDate = {
    implicit val holidaySet: BankHolidaySet = bankHolidays
    date.plusWorkingDays(days)
  }

  def bankHolidays: BankHolidaySet =  {
        Await.result(bankHolidaysService.fetchBankHolidaySet, 5.seconds)
  }

  def currentDateTime: LocalDateTime = SystemDate.getSystemDate

  private def getDaysInAdvance(currentHour: Int): Int = {
    implicit val holidaySet: BankHolidaySet = bankHolidays

    if (LocalDateWithHolidays(currentLocalDate).isWorkingDay) {
      if (currentHour >= dayEndHour) 3 else 2
    } else {
      3
    }
  }

  def addMonths(months: Int): LocalDate = currentLocalDate.plusMonths(months)

  def minusYears(years: Int): LocalDate = currentLocalDate.minusYears(years)

  def currentLocalDate: LocalDate = SystemDate.getSystemDate.toLocalDate

  def dynamicFutureDateExample(anchor: LocalDate = SystemDate.getSystemDate.toLocalDate, displacement: Long = 10): String = {
    anchor plusDays displacement format DateTimeFormatter.ofPattern("d M yyyy")
  }
}
