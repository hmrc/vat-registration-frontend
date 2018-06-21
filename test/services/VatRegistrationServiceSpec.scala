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

import java.time.LocalDate

import connectors._
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.TaxableThreshold
import models.external.CompanyRegistrationProfile
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException, Upstream4xxResponse}

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new VatRegistrationService(
      mockS4LService,
      mockRegConnector,
      mockBrConnector,
      mockCompanyRegConnector,
      mockIIService,
      mockKeystoreConnector,
      mockIIConnector
    )
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorpDate(any(), any())(any()))
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

  "assertFootprintNeeded" should {
    "create a footprint" when {
      "if there is a BR regID and a CT document in the correct state" in new Setup {
        when(mockBrConnector.getBusinessRegistrationID(any()))
          .thenReturn(Future.successful(Some("regId")))

        when(mockCompanyRegConnector.getCompanyProfile(any())(any()))
          .thenReturn(Future.successful(Some(CompanyRegistrationProfile("test", Some("04")))))

        mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))

        when(mockIIService.getIncorpDate(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(testIncorporationInfo.statusEvent.incorporationDate))
        when(mockRegConnector.createNewRegistration(any(), any())).thenReturn(validVatScheme.pure)

        mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))

        when(mockCompanyRegConnector.getTransactionId(any())(any()))
          .thenReturn(Future.successful(validCoHoProfile.transactionId))

        await(service.assertFootprintNeeded) mustBe Some(validVatScheme.id, "transactionId")
      }
    }

    "do not create a footprint" when {
      "if there is no BR regID" in new Setup {
        when(mockBrConnector.getBusinessRegistrationID(any()))
          .thenReturn(Future.successful(None))

        await(service.assertFootprintNeeded) mustBe None
      }

      "if there is no CT document" in new Setup {
        when(mockBrConnector.getBusinessRegistrationID(any()))
          .thenReturn(Future.successful(Some("regId")))

        when(mockCompanyRegConnector.getCompanyProfile(any())(any()))
          .thenReturn(Future.successful(None))

        await(service.assertFootprintNeeded) mustBe None
      }


      "if the document is ETMP rejected with a ETMP status of 06" in new Setup {
        when(mockBrConnector.getBusinessRegistrationID(any()))
          .thenReturn(Future.successful(Some("regId")))
        when(mockIIService.getIncorpDate(any(), any())(any[HeaderCarrier]()))
          .thenReturn(Future.successful(testIncorporationInfo.statusEvent.incorporationDate))
        when(mockRegConnector.createNewRegistration(any(), any())).thenReturn(validVatScheme.pure)

        when(mockCompanyRegConnector.getCompanyProfile(any())(any()))
          .thenReturn(Future.successful(Some(CompanyRegistrationProfile("submitted", Some("06")))))

        await(service.assertFootprintNeeded) mustBe None
      }

      Seq(
        "draft", "locked", "rejected"
      ) foreach { status =>
        s"if the document is $status" in new Setup {
          when(mockBrConnector.getBusinessRegistrationID(any()))
            .thenReturn(Future.successful(Some("regId")))

          when(mockCompanyRegConnector.getCompanyProfile(any())(any()))
            .thenReturn(Future.successful(Some(CompanyRegistrationProfile(status, None))))

          await(service.assertFootprintNeeded) mustBe None
        }
      }
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

      when(mockIIConnector.cancelSubscription(any())(any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.submitRegistration()) mustBe Success
    }

    "return a submission retryable on cancel failure" in new Setup {
      when(mockRegConnector.submitRegistration(any())(any()))
        .thenReturn(Future.successful(Success))

      when(mockIIConnector.cancelSubscription(any())(any()))
        .thenReturn(Future.failed(Upstream4xxResponse("400", 400, 400)))

      await(service.submitRegistration()) mustBe SubmissionFailedRetryable
    }
  }

  "Calling getTaxableThreshold" must {
    val taxableThreshold = TaxableThreshold("50000", LocalDate.of(2018, 1, 1).toString)

    "return a taxable threshold" in new Setup {
      when(mockRegConnector.getTaxableThreshold(any())(any())) thenReturn Future.successful(taxableThreshold)
      await(service.getTaxableThreshold(date)) mustBe formattedThreshold
    }
  }
  "getEligibilityData" should {
    "return a JsObject" in new Setup {
      val json = Json.obj("foo" -> "bar")
      when(mockRegConnector.getEligibilityData) thenReturn Future.successful(json)

      await(service.getEligibilityData) mustBe json

    }
    "return an exception if the vat reg connector returns an exception" in new Setup {
      when(mockRegConnector.getEligibilityData) thenReturn Future.failed(new Exception(""))

      intercept[Exception](await(service.getEligibilityData))
    }
  }
}