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

package services

import connectors.BankHolidaysConnectorV2
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.libs.json._
import services.BankHolidaysService.bankHolidaySetFormat
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.mongo.cache.CacheItem
import utils.workingdays.BankHolidaySet

import java.time.Instant
import scala.concurrent.Future

class BankHolidaysServiceSpec
  extends PlaySpec
    with MockitoSugar
    with ScalaFutures {

  implicit val ec = scala.concurrent.ExecutionContext.global

  val connector = mock[BankHolidaysConnectorV2]
  val service = new BankHolidaysService(connector)

  val sampleSet =
    BankHolidaySet("england-and-wales", List.empty)

  val cacheItem = CacheItem(
    id = "all_users",
    data = JsObject.empty,
    createdAt = Instant.now(),
    modifiedAt = Instant.now()
  )

  "fetchBankHolidaySet" should {

    "return cached value when present" in {

      when(connector.getBankHolidaysFromCache[BankHolidaySet]())
        .thenReturn(Future.successful(Some(sampleSet)))

      val result = service.fetchBankHolidaySet

      whenReady(result) { res =>
        res mustBe sampleSet
      }
    }

    "fetch from API when cache is empty and save to cache" in {

      val httpResponse = mock[HttpResponse]
      when(httpResponse.status).thenReturn(OK)
      when(httpResponse.json).thenReturn(
        Json.obj(
          "england-and-wales" -> Json.obj(
            "events" -> Json.arr()
          ),
          "scotland" -> Json.obj("events" -> Json.arr()),
          "northern-ireland" -> Json.obj("events" -> Json.arr())
        )
      )

      when(connector.getBankHolidaysFromCache[BankHolidaySet]())
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.successful(httpResponse))

      when(connector.saveBankHolidaysDataOnCache(any[BankHolidaySet])(
        any()
      )).thenReturn(Future.successful(cacheItem))

      val result = service.fetchBankHolidaySet

      whenReady(result) { res =>
        res.division mustBe "england-and-wales"
        res.events mustBe Nil
      }
    }

    "fail when cache empty and API fails" in {

      when(connector.getBankHolidaysFromCache[BankHolidaySet]())
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.failed(new RuntimeException("API down")))

      val result = service.fetchBankHolidaySet

      recoverToSucceededIf[Throwable] {
        result
      }
    }
  }
}