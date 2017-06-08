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

import common.exceptions.DownstreamExceptions.RegistrationIdNotFoundException
import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.VatRegSpec
import models.view.vatTradingDetails.vatChoice.StartDateView
import models.{S4LKey, ViewModelFormat}
import org.mockito.Matchers.{any, eq => =~=}
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap


class S4LServiceSpec extends VatRegSpec with S4LFixture with VatRegistrationFixture {

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
      override val keystoreConnector = mockKeystoreConnector
    }

    val key = TestGroup.s4lKey.key
    reset(mockS4LConnector)

  }

  val tstStartDateModel = StartDateView("", None)

  /*"S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      mockS4LSaveForm[StartDateView](cacheMap)
      service.save[StartDateView](tstStartDateModel) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LFetchAndGet[StartDateView](S4LKey[StartDateView].key, Some(tstStartDateModel))
      service.fetchAndGet[StartDateView]() returns Some(tstStartDateModel)
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      mockS4LClear()
      service.clear().map(_.status) returns 200
    }

    "fetch all data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      private val cacheMap = CacheMap("allData", Map.empty)
      mockS4LFetchAll(Some(cacheMap))
      service.fetchAll() returns Some(cacheMap)
    }

  }*/

  "getting a View Model from Save 4 Later" should {

    "fail with an exception when registration ID cannot be found in keystore" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", None)
      service.getViewModel[TestView, TestGroup]() failedWith classOf[RegistrationIdNotFoundException]
    }

    "return None if not in S4L" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any()))
        .thenReturn(Option.empty.pure)
      service.getViewModel[TestView, TestGroup]().returnsNone
    }

    "return None if container object in S4l does not contain requested View" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any()))
        .thenReturn(Some(TestGroup()).pure)
      service.getViewModel[TestView, TestGroup]().returnsNone
    }

    "return a view model if container object in S4l contains requested View" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
      when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any()))
        .thenReturn(Some(TestGroup(testView = Some(TestView("test")))).pure)
      service.getViewModel[TestView, TestGroup]() returnsSome TestView("test")
    }

  }

  "updating a View Model in Save 4 Later" should {

    val cacheMap = CacheMap("id", Map())
    val testView = TestView("test")


    "fail with an exception when registration ID cannot be found in keystore" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", None)
      service.updateViewModel[TestView, TestGroup](testView) failedWith classOf[RegistrationIdNotFoundException]
    }

    "save test view in appropriate container object in Save 4 Later" when {

      "no container in s4l" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any())).thenReturn(Option.empty.pure)
        when(mockS4LConnector.save(=~=(validRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView) returns cacheMap
        verify(mockS4LConnector).save(validRegId, key, TestGroup(Some(testView)))
      }

      "container in s4l does not already contain the view" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any())).thenReturn(Option(TestGroup()).pure)
        when(mockS4LConnector.save(=~=(validRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView) returns cacheMap
        verify(mockS4LConnector).save(validRegId, key, TestGroup(Some(testView)))
      }

      "container in s4l already contains the view" in new Setup {
        mockKeystoreFetchAndGet[String]("RegistrationId", Some(validRegId))
        when(mockS4LConnector.fetchAndGet[TestGroup](=~=(validRegId), =~=(key))(any(), any()))
          .thenReturn(Option(TestGroup(Some(TestView("oldProperty")))).pure)
        when(mockS4LConnector.save(=~=(validRegId), =~=(key), any())(any(), any())).thenReturn(cacheMap.pure)

        service.updateViewModel[TestView, TestGroup](testView) returns cacheMap
        verify(mockS4LConnector).save(validRegId, key, TestGroup(Some(testView)))
      }

    }

  }


}
