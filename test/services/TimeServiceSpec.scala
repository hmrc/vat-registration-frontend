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

import org.joda.time.{LocalDate => JodaLocalDate, LocalDateTime => JodaLocalDateTime}
import java.time.{LocalDate => JavaLocalDate}

import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.inject.ServicesConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import scala.concurrent.Future

class TimeServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  def timeServiceMock(dateTime: JodaLocalDateTime, dayEnd: Int, bankHolidayDates : List[BankHoliday]) = new TimeService {
    override val dayEndHour: Int = dayEnd
    override def currentDateTime: JodaLocalDateTime = dateTime
    override def currentLocalDate: JodaLocalDate = currentDateTime.toLocalDate
    implicit val bankHolidaySet: BankHolidaySet = BankHolidaySet("england-and-wales", bankHolidayDates)
  }

  "TimeService" should {

    val bhDud = new BankHoliday(title="testBH", date=new JodaLocalDate(2000, 10, 10))
    val bh3rd = new BankHoliday(title="testBH", date=new JodaLocalDate(2017, 1, 3))
    val bh6th = new BankHoliday(title="testBH", date=new JodaLocalDate(2017, 1, 6))
    val bh9th = new BankHoliday(title="testBH", date=new JodaLocalDate(2017, 1, 9))

    // Before 2pm, no bank holiday
    "return true when a date 3 days away is supplied before 2pm and does not conflict with any bank holidays"in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date is 2 days away is supplied before 2pm and does not conflict with any bank holidays"in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bhDud))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 3))(ts.bankHolidaySet) shouldBe false
    }


    // After 2pm, no bank holiday
    "return true when a date 4 days away is supplied after 2pm and does not conflict with any bank holidays"in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 15, 0), 14, List(bhDud))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 6))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date 3 days away is supplied after 2pm and does not conflict with any bank holidays"in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 15, 0), 14, List(bhDud))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) shouldBe false
    }


    // Before 2pm, bank holiday
    "return true when a date 3 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bh9th))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 5))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date 2 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 2, 12, 0), 14, List(bh3rd))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 4))(ts.bankHolidaySet) shouldBe false
    }


    // Weekend, no bank holiday
    "return true when a date is a saturday and the date entered is a wednesday and no bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bhDud))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 12))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date is a saturday and the date entered is a tuesday and no bank holiday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bhDud))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 10))(ts.bankHolidaySet) shouldBe false
    }


    // Weekend, bank holiday monday
    "return true when a date is a saturday and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bh9th))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date is a saturday and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 12, 0), 14, List(bh9th))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) shouldBe false
    }


    // Weekend, bank holiday monday, after 2pm
    "return true when a date is a saturday and it is submitted after 2pm and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 15, 0), 14, List(bh9th))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date is a saturday and it is submitted after 2pm and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 7, 15, 0), 14, List(bh9th))
        ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) shouldBe false
    }


    // Thursday, bank holiday friday, bank holiday monday, after 2pm
    "return true when a date is a thursday and it is submitted after 2pm and the date entered is the next thursday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 13))(ts.bankHolidaySet) shouldBe true
    }

    "return false when a date is a thursday and it is submitted after 2pm and the date entered is the next wednesday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(JavaLocalDate.of(2017, 1, 11))(ts.bankHolidaySet) shouldBe false
    }


    //Test the future dates
    val ts = timeServiceMock(new JodaLocalDateTime(2017, 1, 5, 15, 0), 14, List(bh3rd, bh6th, bh9th))
    implicit val bHSTest: BankHolidaySet = ts.bankHolidaySet

    "return a future date " in {
      ts.futureWorkingDate(JodaLocalDate.parse("2016-12-13"), 60)(bHSTest) shouldBe "10 03 2017"
    }
    "return a future date ignoring bank holidays" in {
      ts.futureWorkingDate(JodaLocalDate.parse("2017-04-13"), 1)(bHSTest) shouldBe "14 04 2017"
    }
    "return a future date ignoring bank holidays 2 working days in the future" in {
      ts.futureWorkingDate(JodaLocalDate.parse("2017-04-13"), 2)(bHSTest) shouldBe "17 04 2017"
    }
  }

  "dynamicDateExample" must {
    import java.time.LocalDate.{of => d}
    val mockEnv = mock[Environment]
    val mockServicesConfig = mock[ServicesConfig]
    val service = new TimeServiceImpl(mockEnv, mockServicesConfig)

    "return a date 10 calendar days in the future" in {
      val testCases = Seq(
        d(2016,1,1)   ->  "11 1 2016",
        d(2016,2,19)  ->  "29 2 2016",
        d(2017,2,19)  ->  "1 3 2017",
        d(2016,12,22) ->  "1 1 2017"
      )

      testCases foreach { case (testInput, expectedOutput) =>
        service.dynamicFutureDateExample(testInput) shouldBe expectedOutput
      }
    }

    "return a date which is a specified number of calendar days in the future" in {
      service.dynamicFutureDateExample(d(2016,1,1), 22) shouldBe "23 1 2016"
    }
  }
}
