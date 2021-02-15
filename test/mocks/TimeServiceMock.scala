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

package mocks

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import services.TimeService
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import java.time.{LocalDate, LocalDateTime}

trait TimeServiceMock {
  import org.joda.time.{LocalDate => JodaLocalDate, LocalDateTime => JodaLocalDateTime}

  val mockTimeService: TimeService

  def mockAllTimeService(date: LocalDateTime, minAdditionalDayInFuture: Int): OngoingStubbing[BankHolidaySet] = {
    val ldt = JodaLocalDateTime.parse(date.toString)
    val ld  = ldt.toLocalDate

    when(mockTimeService.currentLocalDate)
      .thenReturn(ld)
    when(mockTimeService.currentDateTime)
      .thenReturn(ldt)
    when(mockTimeService.dayEndHour)
      .thenReturn(14)
    when(mockTimeService.getMinWorkingDayInFuture(any()))
      .thenReturn(LocalDate.parse(ld.plusDays(minAdditionalDayInFuture).toString))
    when(mockTimeService.addMonths(any()))
      .thenReturn(LocalDate.parse(ld.plusMonths(3).toString))
    when(mockTimeService.minusYears(any()))
      .thenReturn(LocalDate.parse(ld.minusYears(4).toString))
    when(mockTimeService.bankHolidaySet)
      .thenReturn(BankHolidaySet("england-and-wales", List(
        BankHoliday(title = "Good Friday",            date = new JodaLocalDate(2017, 4, 14)),
        BankHoliday(title = "Easter Monday",          date = new JodaLocalDate(2017, 4, 17)),
        BankHoliday(title = "Early May bank holiday", date = new JodaLocalDate(2017, 5, 1)),
        BankHoliday(title = "Spring bank holiday",    date = new JodaLocalDate(2017, 5, 29)),
        BankHoliday(title = "Summer bank holiday",    date = new JodaLocalDate(2017, 8, 28)),
        BankHoliday(title = "Christmas Day",          date = new JodaLocalDate(2017, 12, 25)),
        BankHoliday(title = "Boxing Day",             date = new JodaLocalDate(2017, 12, 26)),
        BankHoliday(title = "New Year's Day",         date = new JodaLocalDate(2018, 1, 1))
      )))
  }
}
