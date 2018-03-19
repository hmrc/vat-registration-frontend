/*
 * Copyright 2018 HM Revenue & Customs
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

import java.time.LocalDate.now
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Named}

import common.DateConversions._
import connectors.BankHolidaysConnector
import play.api.cache.CacheApi
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

class DateServiceImpl @Inject()(val bankHolidaysConnector: BankHolidaysConnector,
                                val cache: CacheApi,
                                @Named("fallback") val fallbackBHConnector: BankHolidaysConnector) extends DateService

trait DateService {
  val cache: CacheApi
  val fallbackBHConnector: BankHolidaysConnector
  val bankHolidaysConnector: BankHolidaysConnector

  private val BANK_HOLIDAYS_CACHE_KEY = "bankHolidaySet"

  private val defaultHolidaySet: BankHolidaySet = Await.result(fallbackBHConnector.bankHolidays()(HeaderCarrier()), 1 second)

  def addWorkingDays(date: LocalDate, days: Int): LocalDate = {
    implicit val hols: BankHolidaySet = cache.getOrElse[BankHolidaySet](BANK_HOLIDAYS_CACHE_KEY, 1 day) {
      logger.info(s"Reloading cache entry for $BANK_HOLIDAYS_CACHE_KEY")
      Try {
        Await.result(bankHolidaysConnector.bankHolidays()(HeaderCarrier()), 5 seconds)
      }.getOrElse {
        logger.error("Failed to load bank holidays schedule from BankHolidaysConnector, using default bank holiday set")
        defaultHolidaySet
      }
    }

    (date: org.joda.time.LocalDate).plusWorkingDays(days)
  }

  def dynamicFutureDateExample(anchor: LocalDate = now, displacement: Long = 10): String = {
    anchor plusDays displacement format DateTimeFormatter.ofPattern("d M yyyy")
  }

}
