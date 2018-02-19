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

import connectors._
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.external.IncorporationInfo
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new VatRegistrationService(
      mockS4LService,
      mockRegConnector,
      mockCompanyRegConnector,
      mockIIService,
      mockKeystoreConnector,
      mockTurnoverEstimatesService
    )
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorporationInfo(any())(any()))
      .thenReturn(Future.successful(None))
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

  "Calling createNewRegistration" should {
    "return a success response when the Registration is successfully created" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))

      when(mockIIService.getIncorporationInfo(any())(any[HeaderCarrier]()))
        .thenReturn(Future.successful(Some(testIncorporationInfo)))
      when(mockRegConnector.createNewRegistration(any(), any())).thenReturn(validVatScheme.pure)

      mockKeystoreCache[IncorporationInfo]("INCORPORATION_STATUS", CacheMap("INCORPORATION_STATUS", Map("INCORPORATION_STATUS" -> json)))

      mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))

      when(mockCompanyRegConnector.getTransactionId(any())(any()))
        .thenReturn(Future.successful(validCoHoProfile.transactionId))

      when(mockCompanyRegConnector.getCTStatus(any())(any()))
        .thenReturn(Future.successful(Some("04")))

      await(service.createRegistrationFootprint) mustBe (validVatScheme.id, "transactionId", Some("04"))
    }
  }

  "Calling getAckRef" should {
    "retrieve Acknowledgement Reference (id) from the backend" in new Setup {
      when(mockRegConnector.getAckRef(ArgumentMatchers.eq(testRegId))(any()))
        .thenReturn(Future.successful("testRefNo"))

      await(service.getAckRef(testRegId)) mustBe "testRefNo"
    }
    "retrieve no Acknowledgement Reference if there's none in the backend" in new Setup {
      when(mockRegConnector.getAckRef(ArgumentMatchers.eq(testRegId))(any()))
        .thenReturn(Future.failed(new InternalServerException("")))

      intercept[InternalServerException](await(service.getAckRef(testRegId)))
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(any())(any(), any())).thenReturn(Future.successful(true))

      await(service.deleteVatScheme) mustBe true
    }
  }

  "Calling submitRegistration" should {
    "return a Success DES response" in new Setup {
      when(mockRegConnector.submitRegistration(any())(any()))
        .thenReturn(Future.successful(Success))
      await(service.submitRegistration()) mustBe Success
    }
  }
}