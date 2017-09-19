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

import java.time.LocalDate

import cats.data.OptionT
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models._
import models.api._
import models.external.{CoHoCompanyProfile, IncorporationInfo}
import models.view.frs._
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.ppob.PpobView
import models.view.vatFinancials.ZeroRatedSales
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.vatChoice.StartDateView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.Future
import scala.language.postfixOps

class VatRegistrationServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service = new VatRegistrationService(mockS4LService, mockRegConnector, mockCompanyRegConnector, mockIIService) {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorporationInfo(any())(any())).thenReturn(OptionT.none[Future, IncorporationInfo])
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
      when(mockIIService.getIncorporationInfo(any())(any[HeaderCarrier]())).thenReturn(OptionT.liftF(Future.successful(testIncorporationInfo)))
      when(mockRegConnector.createNewRegistration()(any(), any())).thenReturn(validVatScheme.pure)
      mockKeystoreCache[IncorporationInfo]("INCORPORATION_STATUS", CacheMap("INCORPORATION_STATUS", Map("INCORPORATION_STATUS" -> json)))
      mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))
      when(mockCompanyRegConnector.getTransactionId(any())(any())).thenReturn(Future.successful(validCoHoProfile.transactionId))

      await(service.createRegistrationFootprint()) mustBe (validVatScheme.id, "transactionId")
    }
  }

//  "Calling createNewRegistration" should {
//    "return a success response when the Registration is successfully created without finding a company profile" in new Setup {
//      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
//      when(mockIIService.getIncorporationInfo(any())(any[HeaderCarrier]())).thenReturn(OptionT.liftF(Future.successful(testIncorporationInfo)))
//      when(mockRegConnector.createNewRegistration()(any(), any())).thenReturn(validVatScheme.pure)
//      mockKeystoreCache[IncorporationInfo]("INCORPORATION_STATUS", CacheMap("INCORPORATION_STATUS", Map("INCORPORATION_STATUS" -> json)))
//      mockKeystoreCache[String]("CompanyProfile", CacheMap("", Map.empty))
//      when(mockCompanyRegConnector.getTransactionId(any())(any())).thenReturn(Future.failed(throw new InternalServerException("")))
//
//      await(service.createRegistrationFootprint()) mustBe validVatScheme.id
//    }
//  }

  "Calling getAckRef" should {
    "retrieve Acknowledgement Reference (id) from the backend" in new Setup {
      when(mockRegConnector.getAckRef(Matchers.eq(testRegId))(any())).thenReturn(OptionT.some("testRefNo"))
      service.getAckRef(testRegId) returnsSome "testRefNo"
    }
    "retrieve no Acknowledgement Reference if there's none in the backend" in new Setup {
      when(mockRegConnector.getAckRef(Matchers.eq(testRegId))(any())).thenReturn(OptionT.none[Future, String])
      service.getAckRef(testRegId) returnsNone
    }
  }

  "Calling submitSicAndCompliance" should {
    "return a success response when SicAndCompliance is submitted" in new Setup {
      save4laterReturnsNothing[S4LVatSicAndCompliance]()
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }

    "return a success response when SicAndCompliance is submitted for the first time" in new Setup {
      save4laterReturns(S4LVatSicAndCompliance(
        description = Some(BusinessActivityDescription("bad")),
        mainBusinessActivity = Some(MainBusinessActivityView(id = "mba", mainBusinessActivity = Some(sicCode)))))

      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertSicAndCompliance(any(), any())(any(), any())).thenReturn(validSicAndCompliance.pure)

      service.submitSicAndCompliance() returns validSicAndCompliance
    }
  }

  "Calling submitEligibility" should {
    "return a success response when VatEligibility is submitted" in new Setup {
      save4laterReturns(S4LVatEligibility(Some(validServiceEligibility)))

      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(validVatScheme.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)

      service.submitVatEligibility() returns validServiceEligibility
    }
  }

  "Calling deleteVatScheme" should {
    "return a success response when the delete VatScheme is successful" in new Setup {
      mockKeystoreCache[String]("RegistrationId", CacheMap("", Map.empty))
      when(mockRegConnector.deleteVatScheme(any())(any(), any())).thenReturn(().pure)

      service.deleteVatScheme() completedSuccessfully
    }
  }

  "When this is the first time the user starts a journey and we're persisting to the backend" should {

    "submitTradingDetails should fail if there's not trace of VatTradingDetails in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LTradingDetails]()

      service.submitTradingDetails() failedWith classOf[IllegalStateException]
    }

    "submitVatContact should process the submission even if VatScheme does not contain a VatContact object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatContact(any(), any())(any(), any())).thenReturn(validVatContact.pure)
      save4laterReturns(S4LVatContact(
        businessContactDetails = Some(validBusinessContactDetails),
        ppob = Some(PpobView(addressId = "id", address = Some(scrsAddress)))))

      service.submitVatContact() returns validVatContact
    }

    "submitVatContact should fail if there's not trace of VatContact in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatContact]()

      service.submitVatContact() failedWith classOf[IllegalStateException]
    }

    "submitVatEligibility should process the submission even if VatScheme does not contain a VatEligibility object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatEligibility(any(), any())(any(), any())).thenReturn(validServiceEligibility.pure)
      save4laterReturns(S4LVatEligibility(Some(validServiceEligibility)))
      service.submitVatEligibility() returns validServiceEligibility
    }

    "submitVatEligibility should fail if there's not trace of VatEligibility in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatEligibility]()

      service.submitVatEligibility() failedWith classOf[IllegalStateException]
    }

    "submitVatLodgingOfficer should process the submission even if VatScheme does not contain a VatLodgingOfficer object" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatLodgingOfficer(any(), any())(any(), any())).thenReturn(validLodgingOfficer.pure)
      save4laterReturns(S4LVatLodgingOfficer(
        officerHomeAddress = Some(OfficerHomeAddressView(scrsAddress.id, Some(scrsAddress))),
        officerSecurityQuestions = Some(OfficerSecurityQuestionsView(testDate, testNino)),
        completionCapacity = Some(CompletionCapacityView("id", Some(completionCapacity))),
        officerContactDetails = Some(validOfficerContactDetailsView),
        formerName = Some(FormerNameView(yesNo = false)),
        previousAddress = Some(PreviousAddressView(yesNo = false))
      ))

      service.submitVatLodgingOfficer() returns validLodgingOfficer
    }

    "submitVatLodgingOfficer should fail if there's not trace of VatLodgingOfficer in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatLodgingOfficer]()

      service.submitVatLodgingOfficer() failedWith classOf[IllegalStateException]
    }

    "submitSicAndCompliance should fail if VatSicAndCompliance not in backend and S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatSicAndCompliance]()

      service.submitSicAndCompliance() failedWith classOf[IllegalStateException]
    }
  }
}
