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

package connectors

import itutil.IntegrationSpecBase
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import services.BankHolidaysService.bankHolidaySetFormat
import support.AppAndStubs
import utils.workingdays.{BankHoliday, BankHolidaySet}

import java.time.LocalDate
import scala.concurrent.ExecutionContextExecutor

class BankHolidaysConnectorV2ISpec
  extends IntegrationSpecBase
    with AppAndStubs
    with ScalaFutures
    with Matchers {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  lazy val connector: BankHolidaysConnectorV2 = app.injector.instanceOf[BankHolidaysConnectorV2]

  val cacheId = "all_users"

  val sample: BankHolidaySet =
    BankHolidaySet("england-and-wales", List(BankHoliday("some holiday", LocalDate.of(2026, 4, 6))))

  // save scenarios

  "saveBankHolidaysDataOnCache" should {

    "store data successfully in Mongo cache" in {
      val result =
        connector.saveBankHolidaysDataOnCache(sample).futureValue

      result.id mustBe cacheId
    }

    "overwrite existing cached data with new value" in {
      connector.saveBankHolidaysDataOnCache(sample).futureValue

      val updated =
        sample.copy(events = List.empty)

      val result =
        connector.saveBankHolidaysDataOnCache(updated).futureValue

      result.id mustBe cacheId

      val cached =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      cached mustBe Some(updated)
    }
  }

  // get scenarios

  "getBankHolidaysFromCache" should {

    "return None when cache is empty" in {

      connector.bankHolidayRepository.deleteEntity(cacheId).futureValue

      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      result mustBe None
    }

    "return cached value when present" in {
      connector.saveBankHolidaysDataOnCache(sample).futureValue

      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      result mustBe Some(sample)
    }

    "return updated value after overwrite" in {
      val first =
        sample.copy(events = List.empty)

      val second =
        sample.copy(events = List.empty)

      connector.saveBankHolidaysDataOnCache(first).futureValue
      connector.saveBankHolidaysDataOnCache(second).futureValue

      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      result mustBe Some(second)
    }
  }

  // cache behaviour

  "Bank holidays cache behaviour" should {

    "maintain same cache id across operations" in {

      connector.saveBankHolidaysDataOnCache(sample).futureValue
      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      result.isDefined mustBe true
    }

    "handle empty bank holiday list correctly" in {

      val empty =
        BankHolidaySet("england-and-wales", Nil)

      connector.saveBankHolidaysDataOnCache(empty).futureValue

      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]().futureValue

      result mustBe Some(empty)
      result.get.events mustBe Nil
    }
  }
}