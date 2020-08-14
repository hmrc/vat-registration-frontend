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

import java.time.LocalDate

import connectors._
import fixtures.VatRegistrationFixture
import models.TaxableThreshold
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import testHelpers.{S4LMockSugar, VatRegSpec}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new VatRegistrationService(
      mockS4LService,
      mockVatRegistrationConnector,
      mockKeystoreConnector
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
       |      "callbackUrl":"http://localhost:9896/TODO-CHANGE-THIS"
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

  "Calling getAckRef" should {
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

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockVatRegistrationConnector.deleteVatScheme(any())(any(), any())).thenReturn(Future.successful(true))

      await(service.deleteVatScheme) mustBe true
    }
  }

  "Calling submitRegistration" should {
    "return a Success DES response" in new Setup {
      when(mockVatRegistrationConnector.submitRegistration(any())(any()))
        .thenReturn(Future.successful(Success))

      await(service.submitRegistration()) mustBe Success
    }
  }

  "Calling getTaxableThreshold" must {
    val taxableThreshold = TaxableThreshold("50000", LocalDate.of(2018, 1, 1).toString)

    "return a taxable threshold" in new Setup {
      when(mockVatRegistrationConnector.getTaxableThreshold(any())(any())) thenReturn Future.successful(taxableThreshold)
      await(service.getTaxableThreshold(date)) mustBe formattedThreshold
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
}