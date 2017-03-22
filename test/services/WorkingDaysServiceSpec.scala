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

import connectors.BankHolidaysConnector
import fixtures.VatRegistrationFixture
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.cache.CacheApi
import testHelpers.VatRegSpec
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

import scala.concurrent.duration.Duration

class WorkingDaysServiceSpec extends VatRegSpec with VatRegistrationFixture {

  implicit val hc = HeaderCarrier()
  val mockBankHolidaysConnector = mock[BankHolidaysConnector]
  val mockCacheApi = mock[CacheApi]

  class Setup {
    val service = new WorkingDaysService(mockBankHolidaysConnector, mockCacheApi)
  }

  "addWorkingDays" must {

    "should return Tuesday by adding 1 working day to Monday" ignore {

      //this will hurt Mockito. Can't mock methods with call-by-name parameters
      when(mockCacheApi.getOrElse[BankHolidaySet](
        Matchers.eq(WorkingDaysService.BANK_HOLIDAYS_CACHE_KEY),
        Matchers.any[Duration]())(Matchers.any()))
        .thenReturn(BankHolidaySet("any", List[BankHoliday]()))

      //      import common.DateConversions._
      //
      //      val d1 = LocalDate.of(2017, 3, 20)
      //      val d2 = service.addWorkingDays(d1, 1)
      //      d1.getDayOfWeek shouldBe 1
      //      d2.getDayOfWeek shouldBe 2
    }

  }

}