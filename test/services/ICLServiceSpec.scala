/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.{ICLConnector, KeystoreConnector}
import models.api.SicCode
import models.{BusinessActivities, SicAndCompliance}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future

//connector returns an exception
//exception when parsing the json

class ICLServiceSpec extends VatRegSpec {

  class Setup {

    val jsResponse = Json.obj("journeyStartUri" -> "example.url", "fetchResultsUri" -> "exampleresults.url")
    val jsInvalidResponse = Json.obj("journeyStartUrl" -> "example.url")
    val testService: ICLService = new ICLService(
      mockICLConnector,
      mockServicesConfig,
      mockKeystoreConnector,
      mockSicAndComplianceService,
      mockVatRegistrationConnector
    ) {
      override lazy val vatRedirectUrl: String = "dummy-url"
    }
  }

  val customICLMessages: CustomICLMessages = CustomICLMessages("heading", "lead", "hint")

  "journeySetup" should {
    "return the ICL start url when neither VR or II are prepopped" in new Setup {
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(SicAndCompliance()))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockKeystoreConnector.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res = await(testService.journeySetup(customICLMessages))
      res mustBe "example.url"
    }
    "return the ICL start url when VR codes are prepopped" in new Setup {
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(
          SicAndCompliance(businessActivities = Some(BusinessActivities(List(SicCode("43220", "Plumbing", "")))))
        ))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockKeystoreConnector.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res = await(testService.journeySetup(customICLMessages))
      res mustBe "example.url"
    }
    "return the ICL start url when VR call fails" in new Setup {
      when(mockSicAndComplianceService.getSicAndCompliance(any(), any()))
        .thenReturn(Future.failed(new InternalServerException("VR call failed")))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockKeystoreConnector.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res = await(testService.journeySetup(customICLMessages))
      res mustBe "example.url"
    }
    "return an exception when setting up ICL failed" in new Setup {
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](await(testService.journeySetup(customICLMessages)))
    }
    "return a exception when json was invalid" in new Setup {
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(jsInvalidResponse))
      intercept[Exception](await(testService.journeySetup(customICLMessages)))
    }
    "return an exception when keystore cache is not successful" in new Setup {
      when(mockKeystoreConnector.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(jsResponse))
      intercept[Exception](await(testService.journeySetup(customICLMessages)))
    }
  }

  "getICLSICCodes" should {
    "return list of sic codes when a successful response is returned from the connector with more than 1 SIC code and keystore returns a String" in new Setup {
      val listOfSicCodes = iclMultipleResultsSicCode1 :: iclMultipleResultsSicCode2 :: Nil
      val sicAndCompUpdated = s4lVatSicAndComplianceWithoutLabour.copy(businessActivities = Some(BusinessActivities(listOfSicCodes)))

      when(mockICLConnector.iclGetResult(any[String]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(iclMultipleResults))
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockSicAndComplianceService.updateSicAndCompliance[BusinessActivities]
        (any[BusinessActivities]())(any(), any())).thenReturn(Future.successful(sicAndCompUpdated))

      val res = await(testService.getICLSICCodes())
      res mustBe listOfSicCodes
    }
    "return list of sic code containing 1 sic code when a successful response is returned from the connector with only 1 SIC code and keystore returns a String" in new Setup {
      val listOf1SicCode = iclMultipleResultsSicCode1 :: Nil
      val sicAndComplianceUpdated = s4lVatSicAndComplianceWithoutLabour.copy(businessActivities = Some(BusinessActivities(listOf1SicCode)))
      when(mockICLConnector.iclGetResult(any[String]())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(iclSingleResult))
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockSicAndComplianceService.updateSicAndCompliance[BusinessActivities]
        (any[BusinessActivities]())(any(), any())).thenReturn(Future.successful(sicAndComplianceUpdated))

      val res = await(testService.getICLSICCodes())
      res mustBe listOf1SicCode
    }
    "return an Exception when the provided key cannot be found in keystore" in new Setup {
      when(mockKeystoreConnector.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](await(testService.getICLSICCodes()))
    }
  }

  "constructJsonForJourneySetup" should {
    "construct a json body for starting an ICL journey" when {
      "given custom messages and sic codes" in new Setup {
        testService.constructJsonForJourneySetup(List("12345"), CustomICLMessages("heading", "lead", "hint")) mustBe
          Json.parse(
            """{
              |  "redirectUrl":"dummy-url",
              |  "journeySetupDetails":{
              |    "customMessages":{ "summary" : {
              |      "heading":"heading",
              |      "lead":"lead",
              |      "hint":"hint"
              |    }},
              |    "sicCodes":["12345"]
              |  }
              |}""".stripMargin)
      }

      "given custom messages and no sic codes" in new Setup {
        testService.constructJsonForJourneySetup(Nil, CustomICLMessages("heading", "lead", "hint")) mustBe
          Json.parse(
            """{
              |  "redirectUrl":"dummy-url",
              |  "journeySetupDetails":{
              |    "customMessages":{ "summary" : {
              |      "heading":"heading",
              |      "lead":"lead",
              |      "hint":"hint"
              |    }}
              |    ,
              |    "sicCodes":[]
              |  }
              |}""".stripMargin)
      }
    }
  }
}

