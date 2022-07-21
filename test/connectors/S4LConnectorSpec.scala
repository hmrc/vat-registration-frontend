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

package connectors

import models.Business
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}

import scala.concurrent.Future

class S4LConnectorSpec extends VatRegSpec {

  val mockShortLivedCache = mock[ShortLivedCache]

  object S4LConnectorTest extends S4LConnector(
    mockShortLivedCache
  )

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(validBusiness)))

  "Fetching from save4later" should {
    "return the correct model" in {

      when(mockShortLivedCache.fetchAndGetEntry[Business](ArgumentMatchers.anyString(), ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(validBusiness)))

      val result = S4LConnectorTest.fetchAndGet[Business]("", "")
      await(result) mustBe Some(validBusiness)
    }
  }

  "Saving a model into save4later" should {
    "save the model" in {
      when(mockShortLivedCache.cache[Business](ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(cacheMap))

      val result = S4LConnectorTest.save[Business]("", "", validBusiness)
      await(result) mustBe cacheMap
    }
  }

  "clearing an entry using save4later" should {
    "clear the entry given the user id" in {
      when(mockShortLivedCache.remove(ArgumentMatchers.anyString())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK, "{}")))

      val result = S4LConnectorTest.clear("test")
      await(result).status mustBe HttpResponse(OK, "{}").status
    }
  }

  "fetchAll" should {
    "fetch all entries in S4L" in {
      when(mockShortLivedCache.fetch(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(cacheMap)))

      val result = S4LConnectorTest.fetchAll("testUserId")
      await(result) mustBe Some(cacheMap)
    }
  }
}
