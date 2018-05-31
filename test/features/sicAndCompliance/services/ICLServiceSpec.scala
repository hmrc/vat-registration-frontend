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

package features.sicAndCompliance.services

import connectors.{ICLConnector, KeystoreConnector}
import features.sicAndCompliance.models.OtherBusinessActivities
import helpers.VatRegSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

//connector returns an exception
//exception when parsing the json

class ICLServiceSpec extends VatRegSpec {

    class Setup {

      val jsResponse = Json.obj("journeyStartUri" -> "example.url", "fetchResultsUri" -> "exampleresults.url")
      val jsInvalidResponse = Json.obj("journeyStartUrl" -> "example.url")
      val testService = new ICLService {
        override val iclConnector: ICLConnector = mockICLConnector
        override val keystore: KeystoreConnector = mockKeystoreConnector
        override val sicAndCompliance = mockSicAndComplianceService
        override val vatRedirectUrl: String = "dummy-url"
      }
    }

    "journeySetup" should {
      "return a Future[String]" in new Setup {
        when(mockICLConnector.ICLSetup(any[JsObject]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(jsResponse))
        when(mockKeystoreConnector.cache[String](any(),any())(any(), any()))
          .thenReturn(Future.successful(validCacheMap))
        val res = await(testService.journeySetup())
        res mustBe "example.url"
      }
      "return an exception" in new Setup {
        when(mockICLConnector.ICLSetup(any[JsObject]())(any[HeaderCarrier]()))
          .thenReturn(Future.failed(new Exception))
        intercept[Exception](await(testService.journeySetup()))
      }
      "return a exception with invalid json" in new Setup {
        when(mockICLConnector.ICLSetup(any[JsObject]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(jsInvalidResponse))
        intercept[Exception](await(testService.journeySetup()))

      }
      "return an exception when keystore cache is not successful" in new Setup {
        when(mockKeystoreConnector.cache[String](any(),any())(any(), any()))
          .thenReturn(Future.failed(new Exception))
        when(mockICLConnector.ICLSetup(any[JsObject]())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(jsResponse))
        intercept[Exception](await(testService.journeySetup()))

      }
    }

  "getICLSICCodes" should {
    "return list of sic codes when a successful response is returned from the connector with more than 1 SIC code and keystore returns a String" in new Setup {
      val listOfSicCodes = iclMultipleResultsSicCode1 :: iclMultipleResultsSicCode2 :: Nil
      val sicAndCompUpdated = s4lVatSicAndComplianceWithoutLabour.copy(otherBusinessActivities = Some(OtherBusinessActivities(listOfSicCodes)))

      when(mockICLConnector.ICLGetResult(any[String]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(iclMultipleResults))
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockSicAndComplianceService.updateSicAndCompliance[OtherBusinessActivities]
        (any[OtherBusinessActivities]())(any(),any())).thenReturn(Future.successful(sicAndCompUpdated))

      val res = await(testService.getICLSICCodes())
      res mustBe listOfSicCodes
    }
    "return list of sic code containing 1 sic code when a successful response is returned from the connector with only 1 SIC code and keystore returns a String" in new Setup {
      val listOf1SicCode = iclMultipleResultsSicCode1 :: Nil
      val sicAndComplianceUpdated = s4lVatSicAndComplianceWithoutLabour.copy(otherBusinessActivities = Some(OtherBusinessActivities(listOf1SicCode)))
      when(mockICLConnector.ICLGetResult(any[String]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(iclSingleResult))
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockSicAndComplianceService.updateSicAndCompliance[OtherBusinessActivities]
        (any[OtherBusinessActivities]())(any(),any())).thenReturn(Future.successful(sicAndComplianceUpdated))

      val res = await(testService.getICLSICCodes())
      res mustBe listOf1SicCode
    }
    "return an Exception when the provided key cannot be found in keystore" in new Setup {
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](await(testService.getICLSICCodes()))
    }
  }
}
