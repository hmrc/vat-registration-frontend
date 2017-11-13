/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.LocalDate.{of => d}
import java.util.concurrent.TimeoutException

import connectors.BankHolidaysConnector
import org.joda.time.{LocalDate => JodaDate}
import org.scalamock.scalatest.MockFactory
import org.scalatest.Inspectors
import play.api.cache.CacheApi
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag


class WorkingDaysServiceSpec extends UnitSpec with MockFactory with Inspectors {

  private case class Test(date: LocalDate, daysToAdd: Int, expected: LocalDate)

  private class Setup {
    val mockConnector = mock[BankHolidaysConnector]
    val mockCache = mock[CacheApi]
  }

  val fixedHolidaySet: BankHolidaySet =
    BankHolidaySet("england-and-wales", List(
      BankHoliday("some holiday", new JodaDate(2017, 3, 24)),
      //March 25,26 is weekend
      BankHoliday("some holiday", new JodaDate(2017, 3, 27))
    ))

  "addWorkingDays" must {

    "skip over weekends as well as bank holidays" in {

      val tests = Seq[Test](
        Test(date = d(2017, 3, 22), daysToAdd = 1, expected = d(2017, 3, 23)),
        Test(date = d(2017, 3, 22), daysToAdd = 2, expected = d(2017, 3, 28)),
        Test(date = d(2017, 3, 23), daysToAdd = 1, expected = d(2017, 3, 28)),
        Test(date = d(2017, 3, 23), daysToAdd = 2, expected = d(2017, 3, 29))
      )

      forAll(tests) { test =>
        new Setup {
          (mockCache.getOrElse[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
            .expects(WorkingDaysService.BANK_HOLIDAYS_CACHE_KEY, 1 day, *, *).returns(fixedHolidaySet)
          (mockConnector.bankHolidays(_: String)(_: HeaderCarrier))
            .expects("england-and-wales", *)
            .returns(Future.successful(fixedHolidaySet)).once()

          //must setup mocks prior to calling new constructor, as one mock is called during construction
          val service = new WorkingDaysService(mockConnector, mockCache, mockConnector)
          service.addWorkingDays(test.date, test.daysToAdd) shouldBe test.expected
        }
      }
    }

    "should call bank holiday connector when nothing found in cache" in new Setup {

      (mockCache.getOrElse[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
        .expects(WorkingDaysService.BANK_HOLIDAYS_CACHE_KEY, 1 day, *, *).onCall(product => {
        // call-by-name parameter of type BankHolidaySet will actually become a
        // () => BankHolidaySet, i.e. Function0[BankHolidaySet] at runtime
        // according to http://stackoverflow.com/a/18298495/81520 we need to do this trick:
        product.productElement(2).asInstanceOf[Function0[BankHolidaySet]]()
      })

      (mockConnector.bankHolidays(_: String)(_: HeaderCarrier))
        .expects("england-and-wales", *)
        .returns(Future.successful(fixedHolidaySet)).noMoreThanTwice()

      //must setup mocks prior to calling new constructor, as one mock is called during construction
      val service = new WorkingDaysService(mockConnector, mockCache, mockConnector)

      val date = d(2017, 3, 23)
      service.addWorkingDays(date, 1) shouldBe d(2017, 3, 28)
    }

    "should call bank holiday connector when nothing found in cache and failed to download file from Web" in new Setup {

      /*
      here the sequence of calls is important:
      1.) on creating the service instance a call to FallbackBankHolidaysConnector
          (subtype of BankHolidaysConnector) is made to load the holiday schedule from file on classpath
      2.) then when the cache is consulted, no value is found
      3.) then the _second_ call to BankHolidaysConnector fails with a timed out future
          which will cause the default holiday schedule to be used temporarily
       */
      inSequence {
        (mockConnector.bankHolidays(_: String)(_: HeaderCarrier))
          .expects("england-and-wales", *)
          .returns(Future.successful(fixedHolidaySet))

        (mockCache.getOrElse[BankHolidaySet](_: String, _: Duration)(_: BankHolidaySet)(_: ClassTag[BankHolidaySet]))
          .expects(WorkingDaysService.BANK_HOLIDAYS_CACHE_KEY, 1 day, *, *).onCall(product => {
          product.productElement(2).asInstanceOf[Function0[BankHolidaySet]]()
        })

        (mockConnector.bankHolidays(_: String)(_: HeaderCarrier))
          .expects("england-and-wales", *)
          .returns(Future.failed(new TimeoutException("failed to load from URL")))
      }

      //must setup mocks prior to calling new constructor, as one mock is called during construction
      val service = new WorkingDaysService(mockConnector, mockCache, mockConnector)

      val date = d(2017, 3, 23)
      service.addWorkingDays(date, 1) shouldBe d(2017, 3, 28)
    }

  }

}
