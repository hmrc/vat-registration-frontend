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

package utils.workingdays

import java.time.LocalDate
import scala.annotation.implicitNotFound
import java.time.DayOfWeek._

object WorkingDays {

  @implicitNotFound(msg = "Need to make a BankHolidaySet available in implicit scope")
  implicit class LocalDateWithHolidays(wrapped: LocalDate)(implicit bankHolidays: BankHolidaySet) {

    /**
     * Returns a copy of this date plus the specified number of working days to the LocalDate. Will
     * skip weekends and bank holidays.
     *
     * @param days the amount of days to add, may be negative
     * @return the new LocalDate plus the increased working days
     */
    def plusWorkingDays(days: Int): LocalDate = {
      implicit def nextDateFunction(date: LocalDate, days: Int): LocalDate = date.plusDays(days)

      implicit def negativeDaysFunction(date: LocalDate, days: Int): LocalDate = date.minusWorkingDays(days)

      calculateWorkingDays(days)(nextDateFunction, negativeDaysFunction)
    }

    /**
     * Returns a copy of this date minus the specified number of working days to the LocalDate. Will
     * skip weekends and bank holidays.
     *
     * @param days the amount of days to subtract, may be negative
     * @return the new LocalDate minus the increased working days
     */
    def minusWorkingDays(days: Int): LocalDate = {
      implicit def nextDateFunction(date: LocalDate, days: Int): LocalDate = date.minusDays(days)

      implicit def negativeDaysFunction(date: LocalDate, days: Int): LocalDate = date.plusWorkingDays(days)

      calculateWorkingDays(days)(nextDateFunction, negativeDaysFunction)
    }

    /**
     * Returns the previous working day for the current date. Will skip weekends and bank holidays.
     *
     * @return the LocalDate that represents the previous working day
     */
    def rollBackWorkingDay: LocalDate = {
      implicit def rollingFunction(date: LocalDate, step: Int): LocalDate = date.minusDays(step)

      rollToWorkingDay(wrapped)
    }

    /**
     * Returns the next working day for the current date. Will skip weekends and bank holidays.
     *
     * @return the LocalDate that represents the next working day
     */
    def rollForwardWorkingDay: LocalDate = {
      implicit def rollingFunction(date: LocalDate, step: Int): LocalDate = date.plusDays(step)

      rollToWorkingDay(wrapped)
    }

    def isWeekendDay: Boolean = wrapped.getDayOfWeek == SUNDAY || wrapped.getDayOfWeek == SATURDAY

    def isBankHoliday: Boolean = !bankHolidays.events.filter(_.date == wrapped).isEmpty

    def isWorkingDay: Boolean = !isWeekendDay && !isBankHoliday

    private def calculateWorkingDays(dayCount: Int)(implicit nextDateFunction: (LocalDate, Int) => LocalDate, negativeDaysFunction: (LocalDate, Int) => LocalDate): LocalDate = {
      dayCount match {
        case 0 => wrapped
        case neg if neg < 0 => negativeDaysFunction(wrapped, dayCount.abs)
        case _ => workingDaysHelper(dayCount, wrapped)(nextDateFunction)
      }
    }

    private def workingDaysHelper(dayCount: Int, nextDate: LocalDate)(implicit nextDateFunction: (LocalDate, Int) => LocalDate): LocalDate = {
      dayCount match {
        case 0 if nextDate.isWorkingDay => nextDate
        case 0 => workingDaysHelper(0, nextDateFunction(nextDate, 1))
        case _ if nextDateFunction(nextDate, 1).isWorkingDay => workingDaysHelper(dayCount - 1, nextDateFunction(nextDate, 1))
        case _ => workingDaysHelper(dayCount, nextDateFunction(nextDate, 1))
      }
    }

    private def rollToWorkingDay(date: LocalDate)(implicit rollingFunction: (LocalDate, Int) => LocalDate): LocalDate = {
      def helper(date: LocalDate): LocalDate = {
        val previousDate = rollingFunction(date, 1)
        if (previousDate.isWorkingDay) previousDate
        else helper(previousDate)
      }

      helper(wrapped)
    }

  }

}
