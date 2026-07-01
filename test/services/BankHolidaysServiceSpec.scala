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

import connectors.BankHolidaysConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import repositories.BankHolidayRepository
import services.BankHolidaysService.GDSBankHolidays
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.cache.CacheItem
import utils.workingdays.{BankHoliday, BankHolidaySet}

import java.time.{Instant, LocalDate}
import scala.concurrent.{ExecutionContextExecutor, Future}

class BankHolidaysServiceSpec
  extends PlaySpec
    with MockitoSugar
    with ScalaFutures
    with Matchers with BeforeAndAfterEach {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val gdsBankHolidaysReads: Reads[GDSBankHolidays] = Json.reads[GDSBankHolidays]

  val connector: BankHolidaysConnector = mock[BankHolidaysConnector]
  val repo: BankHolidayRepository = mock[BankHolidayRepository]
  val service =
    new BankHolidaysService(connector, repo)
  val expectedCacheItem: CacheItem =
    CacheItem(
      id = "all_users",
      data = Json.obj(
        "bank_holidays" -> "Test Day,2026-01-01"
      ),
      createdAt = Instant.now(),
      modifiedAt = Instant.now()
    )
  val testDate: LocalDate = LocalDate.of(2026, 1, 1)
  val apiResult: BankHolidaySet = BankHolidaySet(
    "england-and-wales",
    List(BankHoliday("Test Day", testDate))
  )
  val sampleSet: BankHolidaySet =
    BankHolidaySet("england-and-wales", Nil)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector, repo)
  }

  "fetchBankHolidaySet" should {

    "return cached value when present" in {

      when(repo.getBankHolidaysFromCache)
        .thenReturn(Future.successful(Some(sampleSet)))

      val result = service.fetchBankHolidaySet.futureValue

      result mustBe sampleSet

      verify(repo).getBankHolidaysFromCache
      verifyNoInteractions(connector)
    }

    "fetch from API, cache it, and return value when API succeeds" in {

      val jsonBankHolidays =
        Json.obj(
          "england-and-wales" -> Json.obj(
            "events" -> Json.arr(
              Json.obj(
                "title" -> "Test Day",
                "date"  -> "2026-01-01"
              )
            )
          ),
          "scotland" -> Json.obj(
            "events" -> Json.arr()
          ),
          "northern-ireland" -> Json.obj(
            "events" -> Json.arr()
          )
        )

      val httpResponse = mock[uk.gov.hmrc.http.HttpResponse]

      when(repo.getBankHolidaysFromCache)
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.successful(httpResponse))

      when(httpResponse.status)
        .thenReturn(200)

      when(httpResponse.json)
        .thenReturn(jsonBankHolidays)

      when(repo.saveBankHolidaysDataOnCache(any[BankHolidaySet]))
        .thenReturn(Future.successful(expectedCacheItem))

      val result = service.fetchBankHolidaySet.futureValue

      result mustBe apiResult


      verify(repo).getBankHolidaysFromCache
      verify(connector).getBankHolidaysFromApi
      verify(repo).saveBankHolidaysDataOnCache(any())
    }


    "return empty set when API fails" in {

      val apiResponse = mock[HttpResponse]

      when(repo.getBankHolidaysFromCache)
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.successful(apiResponse))

      when(apiResponse.status).thenReturn(500)

      val result = service.fetchBankHolidaySet.futureValue

      result mustBe BankHolidaySet("england-and-wales", Nil)

      verify(repo).getBankHolidaysFromCache
      verify(connector).getBankHolidaysFromApi
      verify(repo, never).saveBankHolidaysDataOnCache(any())
    }


    "return empty set when JSON parsing fails" in {

      val apiResponse = mock[HttpResponse]

      when(repo.getBankHolidaysFromCache)
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.successful(apiResponse))

      when(apiResponse.status).thenReturn(200)
      when(apiResponse.json).thenReturn(Json.obj()) // invalid structure

      val result = service.fetchBankHolidaySet.futureValue

      result mustBe BankHolidaySet("england-and-wales", Nil)

      verify(repo).getBankHolidaysFromCache
      verify(connector).getBankHolidaysFromApi
    }


    "return empty set when connector throws exception" in {

      when(repo.getBankHolidaysFromCache)
        .thenReturn(Future.successful(None))

      when(connector.getBankHolidaysFromApi)
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result = service.fetchBankHolidaySet.futureValue

      result mustBe BankHolidaySet("england-and-wales", Nil)

      verify(repo).getBankHolidaysFromCache
      verify(connector).getBankHolidaysFromApi
    }
  }
}