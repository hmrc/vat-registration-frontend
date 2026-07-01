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

package repositories

import itutil.IntegrationSpecBase
import play.api.test.Helpers._
import support.AppAndStubs
import utils.workingdays.{BankHoliday, BankHolidaySet}

import java.time.LocalDate

class BankHolidayRepositoryISpec
  extends IntegrationSpecBase
    with AppAndStubs {

  lazy val repository: BankHolidayRepository =
    app.injector.instanceOf[BankHolidayRepository]
  private val bankHolidaySet =
    BankHolidaySet(
      "england-and-wales",
      List(
        BankHoliday("New Year's Day", LocalDate.of(2026, 1, 1)),
        BankHoliday("Good Friday", LocalDate.of(2026, 4, 3))
      )
    )

  override def beforeEach(): Unit = {
    super.beforeEach()

    await(
      repository.collection
        .drop()
        .toFuture()
    )
  }

  "saveBankHolidaysDataOnCache" should {

    "persist bank holidays in Mongo" in {

      await(repository.saveBankHolidaysDataOnCache(bankHolidaySet))

      val result =
        await(repository.getBankHolidaysFromCache)

      result mustBe Some(bankHolidaySet)
    }
  }

  "getBankHolidaysFromCache" should {

    "return None when cache is empty" in {

      repository.getBankHolidaysFromCache.futureValue mustBe None
    }

    "return cached bank holidays" in {

      await(repository.saveBankHolidaysDataOnCache(bankHolidaySet))

      repository.getBankHolidaysFromCache.futureValue mustBe Some(bankHolidaySet)
    }

    "overwrite an existing cached value" in {

      val updated =
        BankHolidaySet(
          "england-and-wales",
          List(
            BankHoliday("Christmas Day", LocalDate.of(2026, 12, 25))
          )
        )

      await(repository.saveBankHolidaysDataOnCache(bankHolidaySet))
      await(repository.saveBankHolidaysDataOnCache(updated))

      repository.getBankHolidaysFromCache.futureValue mustBe Some(updated)
    }
  }
}