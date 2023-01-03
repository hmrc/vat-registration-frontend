/*
 * Copyright 2023 HM Revenue & Customs
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
import org.scalamock.scalatest.MockFactory
import org.scalatest.Inspectors
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.cache.SyncCacheApi
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.TimeoutException
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

class TimeServiceSpec extends AnyWordSpec with MockFactory with Inspectors with Matchers {

  val mockBankHolidaysConnector: BankHolidaysConnector = mock[BankHolidaysConnector]
  val mockCache: SyncCacheApi = mock[SyncCacheApi]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  def timeServiceMock(dateTime: LocalDateTime, dayEnd: Int, bankHolidayDates: List[BankHoliday]): TimeService =
    new TimeService(mockBankHolidaysConnector, mockCache, mockServicesConfig) {
      override lazy val dayEndHour: Int = dayEnd

      override def currentDateTime: LocalDateTime = dateTime

      override def currentLocalDate: LocalDate = currentDateTime.toLocalDate

      override implicit val bankHolidays: BankHolidaySet = BankHolidaySet("england-and-wales", bankHolidayDates)
    }

  val bhDud: BankHoliday = BankHoliday(title = "testBH", date = LocalDate.of(2000, 10, 10))
  val bh3rd: BankHoliday = BankHoliday(title = "testBH", date = LocalDate.of(2017, 1, 3))
  val bh6th: BankHoliday = BankHoliday(title = "testBH", date = LocalDate.of(2017, 1, 6))
  val bh9th: BankHoliday = BankHoliday(title = "testBH", date = LocalDate.of(2017, 1, 9))

  val service = new TimeService(mockBankHolidaysConnector, mockCache, mockServicesConfig)

  "isDateSomeWorkingDaysInFuture" should {
    // Before 2pm, no bank holiday
    "return true when a date 3 days away is supplied before 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 5)) mustBe true
    }

    "return false when a date is 2 days away is supplied before 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 3)) mustBe false
    }


    // After 2pm, no bank holiday
    "return true when a date 4 days away is supplied after 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 15, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 6)) mustBe true
    }

    "return false when a date 3 days away is supplied after 2pm and does not conflict with any bank holidays" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 15, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 5)) mustBe false
    }


    // Before 2pm, bank holiday
    "return true when a date 3 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 5)) mustBe true
    }

    "return false when a date 2 days away is supplied before 2pm and conflicts with one bank holiday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 2, 12, 0), 14, List(bh3rd))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 4)) mustBe false
    }


    // Weekend, no bank holiday
    "return true when a date is a saturday and the date entered is a wednesday and no bank holiday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 12)) mustBe true
    }

    "return false when a date is a saturday and the date entered is a tuesday and no bank holiday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 12, 0), 14, List(bhDud))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 10)) mustBe false
    }


    // Weekend, bank holiday monday
    "return true when a date is a saturday and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 13)) mustBe true
    }

    "return false when a date is a saturday and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 12, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 11)) mustBe false
    }


    // Weekend, bank holiday monday, after 2pm
    "return true when a date is a saturday and it is submitted after 2pm and the date entered is a thursday with a bank holiday monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 15, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 13)) mustBe true
    }

    "return false when a date is a saturday and it is submitted after 2pm and the date entered is a wednesday with a bank holiday monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 7, 15, 0), 14, List(bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 11)) mustBe false
    }


    // Thursday, bank holiday friday, bank holiday monday, after 2pm
    "return true when a date is a thursday and it is submitted after 2pm and the date entered is the next thursday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 13)) mustBe true
    }

    "return false when a date is a thursday and it is submitted after 2pm and the date entered is the next wednesday with a bank holiday friday and monday" in {
      val ts = timeServiceMock(LocalDateTime.of(2017, 1, 5, 15, 0), 14, List(bh6th, bh9th))
      ts.isDateSomeWorkingDaysInFuture(LocalDate.of(2017, 1, 11)) mustBe false
    }
  }



  "dynamicDateExample" must {
    val service = new TimeService(mockBankHolidaysConnector, mockCache, mockServicesConfig)

    "return a date 10 calendar days in the future" in {
      val testCases = Seq(
        LocalDate.of(2016, 1, 1) -> "11 1 2016",
        LocalDate.of(2016, 2, 19) -> "29 2 2016",
        LocalDate.of(2017, 2, 19) -> "1 3 2017",
        LocalDate.of(2016, 12, 22) -> "1 1 2017"
      )

      testCases foreach { case (testInput, expectedOutput) =>
        service.dynamicFutureDateExample(testInput) mustBe expectedOutput
      }
    }

    "return a date which is a specified number of calendar days in the future" in {
      service.dynamicFutureDateExample(LocalDate.of(2016, 1, 1), 22) mustBe "23 1 2016"
    }
  }

  "addWorkingDays" must {
    case class Test(date: LocalDate, daysToAdd: Int, expected: LocalDate)

    val fixedHolidaySet: BankHolidaySet = BankHolidaySet(
      "england-and-wales",
      List(
        BankHoliday("some holiday", LocalDate.of(2017, 3, 24)),
        //March 25,26 is weekend
        BankHoliday("some holiday", LocalDate.of(2017, 3, 27))
      )
    )

    "skip over weekends as well as bank holidays" in {
      val tests = Seq[Test](
        Test(date = LocalDate.of(2017, 3, 22), daysToAdd = 1, expected = LocalDate.of(2017, 3, 23)),
        Test(date = LocalDate.of(2017, 3, 22), daysToAdd = 2, expected = LocalDate.of(2017, 3, 28)),
        Test(date = LocalDate.of(2017, 3, 23), daysToAdd = 1, expected = LocalDate.of(2017, 3, 28)),
        Test(date = LocalDate.of(2017, 3, 23), daysToAdd = 2, expected = LocalDate.of(2017, 3, 29))
      )

      forAll(tests) { test =>
        (mockCache.getOrElseUpdate[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
          .expects("bankHolidaySet", 1 day, *, *).returns(fixedHolidaySet)

        service.addWorkingDays(test.date, test.daysToAdd) mustBe test.expected
      }
    }

    "should call bank holiday connector when nothing found in cache" in {
      (mockCache.getOrElseUpdate[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
        .expects("bankHolidaySet", 1 day, *, *).onCall(product => {
        // call-by-name parameter of type BankHolidaySet will actually become a
        // () => BankHolidaySet, i.e. Function0[BankHolidaySet] at runtime
        // according to http://stackoverflow.com/a/18298495/81520 we need to do this trick:
        product.productElement(2).asInstanceOf[() => BankHolidaySet]()
      })

      (mockBankHolidaysConnector.bankHolidays(_: String)(_: HeaderCarrier))
        .expects("england-and-wales", *)
        .returns(Future.successful(fixedHolidaySet))

      val date = LocalDate.of(2017, 3, 23)

      service.addWorkingDays(date, 1) mustBe LocalDate.of(2017, 3, 28)
    }

    "should call bank holiday connector when nothing found in cache and failed to download file from Web" in {
      inSequence {
        (mockCache.getOrElseUpdate[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
          .expects("bankHolidaySet", 1 day, *, *).onCall(product => {
          product.productElement(2).asInstanceOf[() => BankHolidaySet]()
        })

        (mockBankHolidaysConnector.bankHolidays(_: String)(_: HeaderCarrier))
          .expects("england-and-wales", *)
          .returns(Future.failed(new TimeoutException("failed to load from URL")))

        (mockBankHolidaysConnector.defaultHolidaySet _: () => BankHolidaySet)
          .expects()
          .returns(fixedHolidaySet)
      }

      val date = LocalDate.of(2017, 3, 23)

      service.addWorkingDays(date, 1) mustBe LocalDate.of(2017, 3, 28)
    }
  }
}
