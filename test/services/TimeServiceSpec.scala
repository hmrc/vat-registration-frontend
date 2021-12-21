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

import org.joda.time.{LocalDate => JodaLocalDate, LocalDateTime => JodaLocalDateTime}
import play.api.Environment
import testHelpers.VatRegSpec
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import java.time.{LocalDate => JavaLocalDate}

class TimeServiceSpec extends VatRegSpec {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    System.clearProperty("feature.system-date")
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    System.clearProperty("feature.system-date")
  }

  val mockEnv = mock[Environment]

  def timeServiceMock(dateTime: JodaLocalDateTime, dayEnd: Int, bankHolidayDates: List[BankHoliday]): TimeService =
    new TimeService(mockEnv, mockServicesConfig) {
      override lazy val dayEndHour: Int = dayEnd

      override def currentDateTime: JodaLocalDateTime = dateTime

      override def currentLocalDate: JodaLocalDate = currentDateTime.toLocalDate

      override lazy val bankHolidaySet: BankHolidaySet = BankHolidaySet("england-and-wales", bankHolidayDates)
    }

  val bhDud = new BankHoliday(title = "testBH", date = new JodaLocalDate(2000, 10, 10))
  val bh3rd = new BankHoliday(title = "testBH", date = new JodaLocalDate(2017, 1, 3))
  val bh6th = new BankHoliday(title = "testBH", date = new JodaLocalDate(2017, 1, 6))
  val bh9th = new BankHoliday(title = "testBH", date = new JodaLocalDate(2017, 1, 9))

  "isDateSomeWorkingDaysInFuture" should {
    // Before 2pm, no bank holiday
    "return true when a date 3 days away is supplied before 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date is 2 days away is supplied before 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 3))(ts.bankHolidaySet) mustBe false
    }


    // After 2pm, no bank holiday
    "return true when a date 4 days away is supplied after 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 15, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 6))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date 3 days away is supplied after 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 15, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) mustBe false
    }


    // Before 2pm, bank holiday
    "return true when a date 3 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date 2 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bh3rd))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 4))(ts.bankHolidaySet) mustBe false
    }


    // Weekend, no bank holiday
    "return true when a date is a saturday and the date entered is a wednesday and no bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 12))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date is a saturday and the date entered is a tuesday and no bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 10))(ts.bankHolidaySet) mustBe false
    }


    // Weekend, bank holiday monday
    "return true when a date is a saturday and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date is a saturday and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) mustBe false
    }


    // Weekend, bank holiday monday, after 2pm
    "return true when a date is a saturday and it is submitted after 2pm and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 15, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date is a saturday and it is submitted after 2pm and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 15, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) mustBe false
    }


    // Thursday, bank holiday friday, bank holiday monday, after 2pm
    "return true when a date is a thursday and it is submitted after 2pm and the date entered is the next thursday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) mustBe true
    }

    "return false when a date is a thursday and it is submitted after 2pm and the date entered is the next wednesday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) mustBe false
    }
  }

  "futureWorkingDate" should {
    val bankHolidayDates = List(bh3rd, bh6th, bh9th)
    implicit val bHSTest: BankHolidaySet = BankHolidaySet("england-and-wales", bankHolidayDates)

    "return a future date " in {
      val ts = timeServiceMock(new JodaLocalDateTime(2016, 12, 13, 15, 0), 14, bankHolidayDates)
      ts.futureWorkingDate(60)(bHSTest) mustBe JavaLocalDate.of(2017, 3, 10)
    }
    "return a future date ignoring bank holidays" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 4, 13, 15, 0), 14, bankHolidayDates)
      ts.futureWorkingDate(1)(bHSTest) mustBe JavaLocalDate.of(2017, 4, 14)
    }
    "return a future date ignoring bank holidays 2 working days in the future" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 4, 13, 15, 0), 14, bankHolidayDates)
      ts.futureWorkingDate(2)(bHSTest) mustBe JavaLocalDate.of(2017, 4, 17)
    }
  }

  "dynamicDateExample" must {
    import java.time.LocalDate.{of => d}

    val service = new TimeService(mockEnv, mockServicesConfig)

    "return a date 10 calendar days in the future" in {
      val testCases = Seq(
        d(2016, 1, 1) -> "11 1 2016",
        d(2016, 2, 19) -> "29 2 2016",
        d(2017, 2, 19) -> "1 3 2017",
        d(2016, 12, 22) -> "1 1 2017"
      )

      testCases foreach { case (testInput, expectedOutput) =>
        service.dynamicFutureDateExample(testInput) mustBe expectedOutput
      }
    }

    "return a date which is a specified number of calendar days in the future" in {
      service.dynamicFutureDateExample(d(2016, 1, 1), 22) mustBe "23 1 2016"
    }
  }
}
