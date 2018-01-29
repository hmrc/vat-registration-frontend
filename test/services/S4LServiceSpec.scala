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

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models._
import models.view.vatFinancials.EstimateZeroRatedSales
import org.mockito.ArgumentMatchers.{any, eq => =~=}
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

class S4LServiceSpec extends VatRegSpec with VatRegistrationFixture {

  private final case class TestView(property: String)
  private final case class TestGroup(testView: Option[TestView] = None)

  private object TestView {
    implicit val fmt = Json.format[TestView]
    implicit val viewModelFormat = ViewModelFormat[TestView, TestGroup](
      readF = (_: TestGroup).testView,
      updateF = (v: TestView, g: Option[TestGroup]) => g.getOrElse(TestGroup()).copy(testView = Some(v))
    )
  }

  private object TestGroup {
    implicit val fmt = Json.format[TestGroup]
    implicit val s4lKey = S4LKey[TestGroup]("testGroupKey")
  }

  trait Setup {
    val service = new S4LService {
      override val s4LConnector = mockS4LConnector
    }

    val key = TestGroup.s4lKey.key
  }

  val zeroRatedTurnoverEstimates = EstimateZeroRatedSales(10000)

  "S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      mockS4LSaveForm[S4LVatFinancials](cacheMap)
      service.save(S4LVatFinancials(zeroRatedTurnoverEstimate = Some(zeroRatedTurnoverEstimates))) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(S4LKey[S4LVatFinancials].key, Some(S4LVatFinancials(zeroRatedTurnoverEstimate = Some(zeroRatedTurnoverEstimates))))
      service.fetchAndGet[S4LVatFinancials] returns Some(S4LVatFinancials(zeroRatedTurnoverEstimate = Some(zeroRatedTurnoverEstimates)))
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LClear()
      service.clear.map(_.status) returns 200
    }
  }

  "updating a View Model in Save 4 Later" should {

    val cacheMap = CacheMap("id", Map())
    val testView = TestView("test")
    val testGroup = TestGroup()

    "save test view in appropriate container object in Save 4 Later" when {
      "no container in s4l" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(testRegId), =~=(key))(any(), any())).thenReturn(Option.empty.pure)
        when(mockS4LConnector.save(=~=(testRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView, testGroup.pure) returns cacheMap
        verify(mockS4LConnector).save(testRegId, key, TestGroup(Some(testView)))
      }

      "container in s4l does not already contain the view" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(testRegId), =~=(key))(any(), any())).thenReturn(Option(TestGroup()).pure)
        when(mockS4LConnector.save(=~=(testRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView, testGroup.pure) returns cacheMap
        verify(mockS4LConnector).save(testRegId, key, TestGroup(Some(testView)))
      }

      "container in s4l already contains the view" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(testRegId), =~=(key))(any(), any()))
          .thenReturn(Option(TestGroup(Some(TestView("oldProperty")))).pure)
        when(mockS4LConnector.save(=~=(testRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView, testGroup.pure) returns cacheMap
        verify(mockS4LConnector).save(testRegId, key, TestGroup(Some(testView)))
      }
    }
  }
}
