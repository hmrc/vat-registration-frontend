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

import config.FrontendAppConfig
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Format, JsObject}
import repositories.BankHolidayRepository
import services.BankHolidaysService.bankHolidaySetFormat
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mongo.cache.{CacheItem, DataKey}
import utils.workingdays.BankHolidaySet

import java.time.Instant
import scala.concurrent.Future

class BankHolidaysConnectorV2Spec
  extends PlaySpec
    with MockitoSugar
    with ScalaFutures {

  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val httpClient = mock[HttpClientV2]
  val repo = mock[BankHolidayRepository]
  val config = mock[FrontendAppConfig]

  val connector = new BankHolidaysConnectorV2(httpClient, config, repo)


  "saveBankHolidaysDataOnCache" should {
    "store data in Mongo" in {

      val data = BankHolidaySet("england-and-wales", List.empty)
      val cacheItem = CacheItem(
        id = "all_users",
        data = JsObject.empty,
        createdAt = Instant.now(),
        modifiedAt = Instant.now()
      )

      when(
        repo.put[BankHolidaySet]("all_users")(
          DataKey[BankHolidaySet]("bank_holidays"),
          data
        )
      ).thenReturn(Future.successful(cacheItem))

      val result =
        connector.saveBankHolidaysDataOnCache(data)(bankHolidaySetFormat)

      whenReady(result) { res =>
        res mustBe cacheItem
      }
    }
  }

  "getBankHolidaysFromCache" should {
    "return cached data if present" in {
      val data = BankHolidaySet("england-and-wales", List.empty)

      when(
        repo.get[BankHolidaySet]("all_users")(
          DataKey[BankHolidaySet]("bank_holidays")
        )
      ).thenReturn(Future.successful(Some(data)))

      val result =
        connector.getBankHolidaysFromCache[BankHolidaySet]()(bankHolidaySetFormat)

      whenReady(result) { res =>
        res mustBe Some(data)
      }
    }
  }
}