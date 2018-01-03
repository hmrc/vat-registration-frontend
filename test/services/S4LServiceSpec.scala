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

import fixtures.{S4LFixture, VatRegistrationFixture}
import helpers.VatRegSpec
import models.api.VatServiceEligibility
import models.view.vatContact.BusinessContactDetails
import models.{S4LKey, S4LVatContact, S4LVatEligibility, ViewModelFormat}
import org.mockito.ArgumentMatchers.{any, eq => =~=}
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

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
  }

  val testServiceEligibility = VatServiceEligibility()

  "S4L Service" should {

    "save a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      private val cacheMap = CacheMap("s-date", Map.empty)
      mockS4LSaveForm[S4LVatEligibility](cacheMap)
      service.save(S4LVatEligibility(vatEligibility = Some(testServiceEligibility))) returns cacheMap
    }

    "fetch a form with the correct key" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LFetchAndGet(S4LKey[S4LVatEligibility].key, Some(S4LVatEligibility(vatEligibility = Some(testServiceEligibility))))
      service.fetchAndGet[S4LVatEligibility] returns Some(S4LVatEligibility(vatEligibility = Some(testServiceEligibility)))
    }

    "clear down S4L data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      mockS4LClear()
      service.clear.map(_.status) returns 200
    }

    "fetch all data" in new Setup {
      mockKeystoreFetchAndGet[String]("RegistrationId", Some(testRegId))
      private val cacheMap = CacheMap("allData", Map.empty)
      mockS4LFetchAll(Some(cacheMap))
      service.fetchAll returns Some(cacheMap)
    }
  }

  "getting a View Model from Save 4 Later" should {
    "yield a None given a unpopulated Container" in new Setup {
      val container = S4LVatContact(None)
      service.getViewModel[BusinessContactDetails, S4LVatContact](Future.successful(container)).returnsNone
    }

    "yield a ViewModel given a populated Container" in new Setup {
      private val contactDetails = BusinessContactDetails(email = "email", daytimePhone = Some("123"), mobile = Some("345"))
      val container = S4LVatContact(Some(contactDetails))

      service.getViewModel[BusinessContactDetails, S4LVatContact](Future.successful(container)) returnsSome contactDetails
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
