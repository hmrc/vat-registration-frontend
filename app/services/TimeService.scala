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

package services

import common.DateConversions._
import connectors.BankHolidaysConnector
import play.api.cache.SyncCacheApi
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.time.DateTimeUtils._
import uk.gov.hmrc.time.workingdays._
import utils.SystemDate

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

@Singleton
class TimeService @Inject()(val bankHolidaysConnector: BankHolidaysConnector,
                            val cache: SyncCacheApi,
                            val servicesConfig: ServicesConfig) {

  val bankHolidaysCacheKey = "bankHolidaySet"

  def bankHolidays: BankHolidaySet = cache.getOrElseUpdate[BankHolidaySet](bankHolidaysCacheKey, 1 day) {
    logger.info(s"Reloading cache entry for $bankHolidaysCacheKey")
    Try {
      Await.result(bankHolidaysConnector.bankHolidays()(HeaderCarrier()), 5 seconds)
    }.getOrElse {
      logger.error("Failed to load bank holidays schedule from BankHolidaysConnector, using default bank holiday set")
      bankHolidaysConnector.defaultHolidaySet
    }
  }

  def addWorkingDays(date: LocalDate, days: Int): LocalDate = {
    implicit val holidaySet: BankHolidaySet = bankHolidays

    javaToJoda(date).plusWorkingDays(days)
  }

  lazy val dayEndHour: Int = servicesConfig.getInt("time-service.day-end-hour")

  def currentDateTime: LocalDateTime = SystemDate.getSystemDate

  def currentLocalDate: LocalDate = SystemDate.getSystemDate.toLocalDate

  def today: LocalDate = LocalDate.now()

  val DATE_FORMAT = "yyyy-MM-dd"

  def isDateSomeWorkingDaysInFuture(futureDate: LocalDate): Boolean = {
    isEqualOrAfter(getMinWorkingDayInFuture, futureDate)
  }

  def getMinWorkingDayInFuture: LocalDate = {
    addWorkingDays(currentLocalDate, getDaysInAdvance(currentDateTime.getHour) + 1)
  }

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

  def dynamicFutureDateExample(anchor: LocalDate = SystemDate.getSystemDate.toLocalDate, displacement: Long = 10): String = {
    anchor plusDays displacement format DateTimeFormatter.ofPattern("d M yyyy")
  }
}
