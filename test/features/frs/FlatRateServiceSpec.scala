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

import cats.data.OptionT
import connectors.{CompanyRegistrationConnector, KeystoreConnector, VatRegistrationConnector}
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LFlatRateScheme
import models.api.VatFlatRateScheme
import models.external.IncorporationInfo
import models.view.frs._
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.language.postfixOps

class FlatRateServiceSpec extends VatRegSpec with S4LMockSugar {

  class Setup {
    val service: FlatRateService = new RegistrationService {
      override val s4LService: S4LService = mockS4LService
      override val vatRegConnector: VatRegistrationConnector = mockRegConnector
      override val compRegConnector: CompanyRegistrationConnector = mockCompanyRegConnector
      override val incorporationService: IncorporationInformationService = mockIIService
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorporationInfo(any())(any())).thenReturn(OptionT.none[Future, IncorporationInfo])
  }

  // TODO old tests - look to change
  "When this is the first time the user starts a journey and we're persisting to the backend" should {
    "submitVatFlatRateScheme should process the submission even if VatScheme does not contain VatFlatRateScheme" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      save4laterReturns(S4LFlatRateScheme(
        joinFrs = Some(JoinFrsView(true)),
        annualCostsInclusive = Some(AnnualCostsInclusiveView("yes")),
        annualCostsLimited = Some(AnnualCostsLimitedView("yes")),
        registerForFrs = Some(RegisterForFrsView(true)),
        frsStartDate = Some(FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE))
      ))
      service.submitVatFlatRateScheme() returns validVatFlatRateScheme
    }

    "submitVatFlatRateScheme should fail if there's no VatFlatRateScheme in backend or S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LFlatRateScheme]()

      service.submitVatFlatRateScheme() failedWith classOf[IllegalStateException]
    }
  }

  "viewToApi" should {

    "transform a S4LFlatRateScheme into a VatFlatRateScheme" in new Setup {
      service.viewToApi(validS4LFlatRateScheme) mustBe validVatFlatRateScheme
    }
  }

  "apiToView" should {

    "transform a VatScheme into a S4LFlatRateScheme if a VatFlatRateScheme exists" in new Setup {
      service.apiToView(validVatScheme) mustBe validS4LFlatRateScheme
    }

    "return an empty S4LFlatRateScheme if the supplied VatScheme does not contain a VatFlatRateScheme" in new Setup {
      service.apiToView(emptyVatScheme) mustBe S4LFlatRateScheme()
    }
  }

  "fetchFlatRateScheme" should {

    "return a S4LFlatRateScheme if one is found in Save 4 Later" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe validS4LFlatRateScheme
    }

    "return a S4LFlatRateScheme if nothing is found in Save 4 Later but one is found in Vat Registration" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe validS4LFlatRateScheme
    }

    "return an empty S4LFlatRateScheme if nothing is found in either Save 4 Later or Vat Registration" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      val result: S4LFlatRateScheme = await(service.fetchFlatRateScheme)

      result mustBe S4LFlatRateScheme()
    }
  }

  "fetchFRSFromS4L" should {

    "return a S4LFlatRateScheme if one is found in Save 4 Later" in new Setup {

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      val result: Option[S4LFlatRateScheme] = await(service.fetchFRSFromS4L)

      result mustBe Some(validS4LFlatRateScheme)
    }

    "return None if a S4LFlatRateScheme is not found in Save 4 Later" in new Setup {

      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result: Option[S4LFlatRateScheme] = await(service.fetchFRSFromS4L)

      result mustBe None
    }
  }

  "fetchFRSFromAPI" should {

    "return a VatFlatRateScheme if a VatScheme is found containing one" in new Setup {
      when(mockRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(validVatScheme))

      val result: Option[VatFlatRateScheme] = await(service.fetchFRSFromAPI)

      result mustBe Some(validVatFlatRateScheme)
    }

    "return None when a VatFlatRateScheme is not found in the returned VatScheme" in new Setup {
      when(mockRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      val result: Option[VatFlatRateScheme] = await(service.fetchFRSFromAPI)

      result mustBe None
    }
  }

  "saveJoinFRS" should {

    "save JoinFrsView To S4L if the logical block is incomplete" in new Setup {
      // TODO this function can only save to S4L at the moment
      when(mockS4LService.fetchAndGetNoAux[S4LFlatRateScheme](any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(validS4LFlatRateScheme)))

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveJoinFRS(JoinFrsView(true)))

      result mustBe Left(validS4LFlatRateScheme)
    }

    "save a FlatRateScheme directly to Vat Registration if JoinFrsView.selection is false" in new Setup {

      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any()))
        .thenReturn(Future.successful(vatFlatRateSchemeNotJoiningFRS))

      val result: Either[_, VatFlatRateScheme] = await(service.saveJoinFRS(JoinFrsView(false)))

      result mustBe Right(vatFlatRateSchemeNotJoiningFRS)
    }
  }

  "saveFRS" should {

    "save to S4L" in new Setup {

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: Either[S4LFlatRateScheme, _] = await(service.saveFRS(validS4LFlatRateScheme))

      result mustBe Left(validS4LFlatRateScheme)
    }
  }

  "saveFRSToS4L" should {

    "return a VatFlatRateScheme that has been saved in Vat Registration" in new Setup {

      when(mockS4LService.saveNoAux[S4LFlatRateScheme](any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(dummyCacheMap))

      val result: S4LFlatRateScheme = await(service.saveFRSToS4L(validS4LFlatRateScheme))

      result mustBe validS4LFlatRateScheme
    }
  }

  "saveFRStoAPI" should {

    "return a VatFlatRateScheme that has been saved in Vat Registration" in new Setup {

      when(mockRegConnector.upsertVatFlatRateScheme(any(), any())(any(), any()))
        .thenReturn(Future.successful(validVatFlatRateScheme))

      val result: VatFlatRateScheme = await(service.saveFRStoAPI(validVatFlatRateScheme))

      result mustBe validVatFlatRateScheme
    }
  }
}
