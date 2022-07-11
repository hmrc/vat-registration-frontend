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

package services

import java.time.LocalDate
import connectors._
import connectors.mocks.MockRegistrationApiConnector
import models.TaxableThreshold
import models.api.EligibilitySubmissionData
import org.mockito
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import testHelpers.{S4LMockSugar, VatRegSpec}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with S4LMockSugar with MockRegistrationApiConnector {

  val testHeaderKey = "testHeaderKey"
  val testHeaderValue = "testHeaderValue"
  implicit val testRequest: FakeRequest[_] = FakeRequest().withHeaders(testHeaderKey -> testHeaderValue)

  class Setup {
    val service = new VatRegistrationService(
      mockS4LService,
      mockVatRegistrationConnector,
      mockRegistrationApiConnector,
      mockSessionService
    )
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
  }

  val json = Json.parse(
    s"""
       |{
       |  "IncorporationInfo":{
       |    "IncorpSubscription":{
       |      "callbackUrl":"http://localhost:9896/callback-url"
       |    },
       |    "IncorpStatusEvent":{
       |      "status":"accepted",
       |      "crn":"90000001",
       |      "description": "Some description",
       |      "incorporationDate":1470438000000
       |    }
       |  }
       |}
        """.stripMargin)

  "getAckRef" should {
    "retrieve Acknowledgement Reference (id) from the backend" in new Setup {
      when(mockVatRegistrationConnector.getAckRef(ArgumentMatchers.eq(testRegId))(any()))
        .thenReturn(Future.successful("testRefNo"))

      await(service.getAckRef(testRegId)) mustBe "testRefNo"
    }
    "retrieve no Acknowledgement Reference if there's none in the backend" in new Setup {
      when(mockVatRegistrationConnector.getAckRef(ArgumentMatchers.eq(testRegId))(any()))
        .thenReturn(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(service.getAckRef(testRegId)))
    }
  }

  "getSection" must {
    "return a section if it exists" in new Setup {
      when(mockRegistrationApiConnector.getSection[EligibilitySubmissionData](ArgumentMatchers.eq(testRegId), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validEligibilitySubmissionData)))

      await(service.getSection[EligibilitySubmissionData](testRegId)) mustBe Some(validEligibilitySubmissionData)
    }
    "return None for a section that doesn't exist" in new Setup {
      when(mockRegistrationApiConnector.getSection[EligibilitySubmissionData](ArgumentMatchers.eq(testRegId), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      await(service.getSection[EligibilitySubmissionData](testRegId)) mustBe None
    }
  }

  "upsertSection" must {
    "return the updated section data" in new Setup {
      when(
        mockRegistrationApiConnector.replaceSection[EligibilitySubmissionData](
          ArgumentMatchers.eq(testRegId),
          ArgumentMatchers.eq(validEligibilitySubmissionData),
          any()
        )(any(), any(), any())
      ).thenReturn(Future.successful(validEligibilitySubmissionData))

      await(service.upsertSection[EligibilitySubmissionData](testRegId, validEligibilitySubmissionData)) mustBe validEligibilitySubmissionData
    }
  }

  "deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      when(mockVatRegistrationConnector.deleteVatScheme(any())(any(), any())).thenReturn(Future.successful(true))

      await(service.deleteVatScheme(testRegId)) mustBe true
    }
  }

  "submitRegistration" should {
    "return a Success DES response" in new Setup {
      when(mockVatRegistrationConnector.submitRegistration(ArgumentMatchers.eq(testRegId), ArgumentMatchers.eq(testRequest.headers.toSimpleMap))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Success))

      await(service.submitRegistration) mustBe Success
    }
  }

  "getTaxableThreshold" must {
    val taxableThreshold = TaxableThreshold("50000", LocalDate.of(2018, 1, 1).toString)

    "return a taxable threshold" in new Setup {
      when(mockVatRegistrationConnector.getTaxableThreshold(any())(any())) thenReturn Future.successful(taxableThreshold)
      await(service.getTaxableThreshold(LocalDate.now)) mustBe formattedThreshold
    }
  }
  "getEligibilityData" should {
    "return a JsObject" in new Setup {
      val json = Json.obj("foo" -> "bar")
      when(mockVatRegistrationConnector.getEligibilityData) thenReturn Future.successful(json)

      await(service.getEligibilityData) mustBe json

    }
    "return an exception if the vat reg connector returns an exception" in new Setup {
      when(mockVatRegistrationConnector.getEligibilityData) thenReturn Future.failed(new Exception(""))

      intercept[Exception](await(service.getEligibilityData))
    }
  }
  "submitHonestyDeclaration" should {
    "return a HttpResponse" in new Setup {
      val httpResponse = Json.obj("honestyDeclartion" -> true)
      val successfulResponse = HttpResponse(200, "{}")
      when(mockVatRegistrationConnector.submitHonestyDeclaration(ArgumentMatchers.eq(testRegId), ArgumentMatchers.eq(testHonestyDeclaration))(any())) thenReturn Future.successful(successfulResponse)

      await(service.submitHonestyDeclaration(testRegId, testHonestyDeclaration)) mustBe successfulResponse
    }

  }
}