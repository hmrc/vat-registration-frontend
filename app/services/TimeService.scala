/*
 * Copyright 2019 HM Revenue & Customs
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

import java.io.InputStream
import java.time.format.DateTimeFormatter
import java.time.{LocalDate => JavaLocalDate, LocalDateTime => JavaLocalDateTime}
import javax.inject.Inject

import org.joda.time.format.DateTimeFormat
import org.joda.time.{LocalDate => JodaLocalDate, LocalDateTime => JodaLocalDateTime}
import play.api.Environment
import play.api.libs.json.{Json, Reads}
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.time.DateTimeUtils._
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet, LocalDateWithHolidays}
import utils.SystemDate

import scala.language.implicitConversions

class TimeServiceImpl @Inject()(val environment: Environment,
                                val servicesConfig: ServicesConfig) extends TimeService {
  override lazy val dayEndHour  = servicesConfig.getInt("time-service.day-end-hour")
  override def currentDateTime  = SystemDate.getSystemDate
  override def currentLocalDate = SystemDate.getSystemDate
  override lazy val bankHolidaySet: BankHolidaySet = {
    implicit val bankHolidayReads: Reads[BankHoliday]       = Json.reads[BankHoliday]
    implicit val bankHolidaySetReads: Reads[BankHolidaySet] = Json.reads[BankHolidaySet]

    val resourceAsStream: InputStream = environment.classLoader.getResourceAsStream("bank-holidays.json")
    //if below .get fails, app startup fails. This is as expected. bank-holidays.json file must be on classpath
    val parsed = Json.parse(resourceAsStream).asOpt[Map[String, BankHolidaySet]].get
    parsed("england-and-wales")
  }
}

trait TimeService {
  implicit def javaLDToJodaLDT(jldt: JavaLocalDateTime): JodaLocalDateTime = JodaLocalDateTime.parse(jldt.toString)
  implicit def javaToJoda(jld: JavaLocalDateTime): JodaLocalDate = JodaLocalDate.parse(jld.toLocalDate.toString)

  implicit def javaToJoda(jld: JavaLocalDate): JodaLocalDate = JodaLocalDate.parse(jld.toString)
  implicit def jodaToJava(jld: JodaLocalDate): JavaLocalDate = JavaLocalDate.parse(jld.toString)

  val DATE_FORMAT = "yyyy-MM-dd"

  val bankHolidaySet: BankHolidaySet

  val dayEndHour: Int

  def currentDateTime: JodaLocalDateTime

  def currentLocalDate: JodaLocalDate

  def isDateSomeWorkingDaysInFuture(futureDate: JavaLocalDate)(implicit bHS: BankHolidaySet): Boolean = {
    isEqualOrAfter(getMinWorkingDayInFuture, futureDate)
  }

  def getMinWorkingDayInFuture(implicit bHS: BankHolidaySet): JavaLocalDate = {
    currentLocalDate.plusWorkingDays(getDaysInAdvance(currentDateTime.getHourOfDay) + 1)
  }

  private def getDaysInAdvance(currentHour: Int)(implicit bHS: BankHolidaySet): Int = {
    if (LocalDateWithHolidays(currentLocalDate).isWorkingDay) {
      if (currentHour >= dayEndHour) 3 else 2
    } else {
      3
    }
  }

  def futureWorkingDate(days: Int)(implicit bHS: BankHolidaySet): JavaLocalDate = currentLocalDate plusWorkingDays days

  def addMonths(months: Int): JavaLocalDate = currentLocalDate.plusMonths(months)

  def dynamicFutureDateExample(anchor: JavaLocalDate = SystemDate.getSystemDate.toLocalDate, displacement: Long = 10): String = {
    anchor plusDays displacement format DateTimeFormatter.ofPattern("d M yyyy")
  }
}
