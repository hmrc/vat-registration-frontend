/*
 * Copyright 2024 HM Revenue & Customs
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

package services.mocks

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import services.TimeService
import utils.workingdays._
import java.time.{LocalDate, LocalDateTime}

trait TimeServiceMock {

  val mockTimeService: TimeService

  def mockAllTimeService(date: LocalDateTime, minAdditionalDayInFuture: Int): OngoingStubbing[BankHolidaySet] = {
    val ldt = LocalDateTime.parse(date.toString)
    val ld = ldt.toLocalDate

    when(mockTimeService.currentLocalDate)
      .thenReturn(ld)
    when(mockTimeService.currentDateTime)
      .thenReturn(ldt)
    when(mockTimeService.dayEndHour)
      .thenReturn(14)
    when(mockTimeService.getMinWorkingDayInFuture)
      .thenReturn(LocalDate.parse(ld.plusDays(minAdditionalDayInFuture).toString))
    when(mockTimeService.addMonths(any()))
      .thenReturn(LocalDate.parse(ld.plusMonths(3).toString))
    when(mockTimeService.minusYears(any()))
      .thenReturn(LocalDate.parse(ld.minusYears(4).toString))
    when(mockTimeService.bankHolidays)
      .thenReturn(BankHolidaySet("england-and-wales", List(
        BankHoliday(title = "Good Friday", date = LocalDate.of(2017,4,14)),
        BankHoliday(title = "Easter Monday", date = LocalDate.of(2017, 4, 17)),
        BankHoliday(title = "Early May bank holiday", date =LocalDate.of(2017, 5, 1)),
        BankHoliday(title = "Spring bank holiday", date = LocalDate.of(2017, 5, 29)),
        BankHoliday(title = "Summer bank holiday", date = LocalDate.of(2017, 8, 28)),
        BankHoliday(title = "Christmas Day", date = LocalDate.of(2017, 12, 25)),
        BankHoliday(title = "Boxing Day", date = LocalDate.of(2017, 12, 26)),
        BankHoliday(title = "New Year's Day", date = LocalDate.of(2018, 1, 1))
      )))
  }

  def mockToday(todaysDate: LocalDate): OngoingStubbing[LocalDate] =
    when(mockTimeService.today).thenReturn(todaysDate)

}
