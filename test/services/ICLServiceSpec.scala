/*
 * Copyright 2024 HM Revenue & Customs
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

import models.Business
import models.api.SicCode
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import services.BusinessService.BusinessActivities
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future
import play.api.mvc.Request
import play.api.test.FakeRequest

//connector returns an exception
//exception when parsing the json

class ICLServiceSpec extends VatRegSpec {

  implicit val fakeRequest: Request[_] = FakeRequest()

  class Setup {

    val jsResponse: JsObject = Json.obj("journeyStartUri" -> "example.url", "fetchResultsUri" -> "exampleresults.url")
    val jsInvalidResponse: JsObject = Json.obj("journeyStartUrl" -> "example.url")
    val testService: ICLService = new ICLService(
      mockICLConnector,
      mockServicesConfig,
      mockSessionService,
      mockBusinessService,
      mockVatRegistrationConnector
    ) {
      override lazy val vatRedirectUrl: String = "dummy-url"
    }
  }

  val customICLMessages: CustomICLMessages = CustomICLMessages(Some("heading"), Some("lead"), Some("hint"))
  val customICLMessagesCy: CustomICLMessages = CustomICLMessages(Some("headingCy"), Some("leadCy"), Some("hintCy"))

  "journeySetup" should {
    "return the ICL start url when neither VR or II are prepopped" in new Setup {
      when(mockBusinessService.getBusiness(any(), any(), any()))
        .thenReturn(Future.successful(validBusinessWithNoDescriptionAndLabour))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockSessionService.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res: String = await(testService.journeySetup(customICLMessages, customICLMessagesCy))
      res mustBe "example.url"
    }
    "return the ICL start url when VR codes are prepopped" in new Setup {
      when(mockBusinessService.getBusiness(any(), any(), any()))
        .thenReturn(Future.successful(
          validBusiness.copy(businessActivities = Some(List(SicCode("43220", "Plumbing", ""))))
        ))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockSessionService.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res: String = await(testService.journeySetup(customICLMessages, customICLMessagesCy))
      res mustBe "example.url"
    }
    "return the ICL start url when VR call fails" in new Setup {
      when(mockBusinessService.getBusiness(any(), any(), any()))
        .thenReturn(Future.failed(new InternalServerException("VR call failed")))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(jsResponse))
      when(mockSessionService.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.successful(validCacheMap))
      val res: String = await(testService.journeySetup(customICLMessages, customICLMessagesCy))
      res mustBe "example.url"
    }
    "return an exception when setting up ICL failed" in new Setup {
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](await(testService.journeySetup(customICLMessages, customICLMessagesCy)))
    }
    "return a exception when json was invalid" in new Setup {
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(jsInvalidResponse))
      intercept[Exception](await(testService.journeySetup(customICLMessages, customICLMessagesCy)))
    }
    "return an exception when keystore cache is not successful" in new Setup {
      when(mockSessionService.cache[String](any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception))
      when(mockICLConnector.iclSetup(any[JsObject]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(jsResponse))
      intercept[Exception](await(testService.journeySetup(customICLMessages, customICLMessagesCy)))
    }
  }

  "getICLSICCodes" should {
    "return list of sic codes when a successful response is returned from the connector with more than 1 SIC code and keystore returns a String" in new Setup {
      val listOfSicCodes: List[SicCode] = iclMultipleResultsSicCode1 :: iclMultipleResultsSicCode2 :: Nil
      val updaetdBusiness: Business = validBusinessWithNoDescriptionAndLabour.copy(businessActivities = Some(listOfSicCodes))

      when(mockICLConnector.iclGetResult(any[String]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(iclMultipleResults))
      when(mockSessionService.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockBusinessService.updateBusiness[BusinessActivities]
        (any[BusinessActivities]())(any(), any(), any())).thenReturn(Future.successful(updaetdBusiness))

      val res: Seq[SicCode] = await(testService.getICLSICCodes())
      res mustBe listOfSicCodes
    }
    "return list of sic code containing 1 sic code when a successful response is returned from the connector with only 1 SIC code and keystore returns a String" in new Setup {
      val listOf1SicCode: List[SicCode] = iclMultipleResultsSicCode1 :: Nil
      val updaetdBusiness: Business = validBusinessWithNoDescriptionAndLabour.copy(businessActivities = Some(listOf1SicCode))
      when(mockICLConnector.iclGetResult(any[String]())(any[HeaderCarrier](), any[Request[_]]()))
        .thenReturn(Future.successful(iclSingleResult))
      when(mockSessionService.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.successful(Some("example.url")))

      when(mockBusinessService.updateBusiness[BusinessActivities]
        (any[BusinessActivities]())(any(), any(), any())).thenReturn(Future.successful(updaetdBusiness))

      val res: List[SicCode] = await(testService.getICLSICCodes())
      res mustBe listOf1SicCode
    }
    "return an Exception when the provided key cannot be found in keystore" in new Setup {
      when(mockSessionService.fetchAndGet[String](any())(any(), any()))
        .thenReturn(Future.failed(new Exception))
      intercept[Exception](await(testService.getICLSICCodes()))
    }
  }

  "constructJsonForJourneySetup" should {
    "construct a json body for starting an ICL journey" when {
      "given custom messages and sic codes" in new Setup {
        testService.constructJsonForJourneySetup(List("12345"), customICLMessages, customICLMessagesCy) mustBe
          Json.parse(
            """{
              |  "redirectUrl":"dummy-url",
              |  "journeySetupDetails":{
              |    "customMessages":{ "summary" : {
              |      "heading":"heading",
              |      "lead":"lead",
              |      "hint":"hint"
              |    },
              |    "summaryCy" : {
              |      "heading":"headingCy",
              |      "lead":"leadCy",
              |      "hint":"hintCy"
              |    }},
              |    "sicCodes":["12345"]
              |  }
              |}""".stripMargin)
      }

      "given custom messages and no sic codes" in new Setup {
        testService.constructJsonForJourneySetup(Nil, customICLMessages, customICLMessagesCy) mustBe
          Json.parse(
            """{
              |  "redirectUrl":"dummy-url",
              |  "journeySetupDetails":{
              |    "customMessages":{ "summary" : {
              |      "heading":"heading",
              |      "lead":"lead",
              |      "hint":"hint"
              |    },
              |    "summaryCy" : {
              |      "heading":"headingCy",
              |      "lead":"leadCy",
              |      "hint":"hintCy"
              |    }},
              |    "sicCodes":[]
              |  }
              |}""".stripMargin)
      }
    }
  }
}

