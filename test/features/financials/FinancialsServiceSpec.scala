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
import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LVatFinancials
import models.api.{VatAccountingPeriod, VatFinancials}
import models.external.IncorporationInfo
import models.view.vatFinancials.ZeroRatedSales
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.Future
import scala.language.postfixOps

class FinancialsServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  class Setup {
    val service: FinancialsService = new RegistrationService {
      override val s4LService = mockS4LService
      override val vatRegConnector = mockRegConnector
      override val compRegConnector = mockCompanyRegConnector
      override val incorporationService = mockIIService
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    super.beforeEach()
    mockFetchRegId(testRegId)
    when(mockIIService.getIncorporationInfo(any())(any())).thenReturn(OptionT.none[Future, IncorporationInfo])
  }

  "When this is the first time the user starts a journey and we're persisting to the backend" should {

    "submitVatFinancials should process the submission even if VatScheme does not contain a VatFinancials object" in new Setup {
      val mergedVatFinancials = VatFinancials(
        bankAccount = Some(validBankAccount),
        turnoverEstimate = validEstimateVatTurnover.vatTurnoverEstimate,
        zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales.zeroRatedTurnoverEstimate),
        reclaimVatOnMostReturns = true,
        accountingPeriods = VatAccountingPeriod("monthly")
      )

      save4laterReturns(S4LVatFinancials(
        estimateVatTurnover = Some(validEstimateVatTurnover),
        zeroRatedTurnover = Some(ZeroRatedSales.yes),
        zeroRatedTurnoverEstimate = Some(validEstimateZeroRatedSales),
        vatChargeExpectancy = Some(validVatChargeExpectancy),
        vatReturnFrequency = Some(validVatReturnFrequency),
        accountingPeriod = Some(validAccountingPeriod),
        companyBankAccount = Some(validCompanyBankAccount),
        companyBankAccountDetails = Some(validBankAccountDetails)))

      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      when(mockRegConnector.upsertVatFinancials(any(), any())(any(), any())).thenReturn(validVatFinancials.pure)

      service.submitVatFinancials() returns mergedVatFinancials
    }

    "submitVatFinancials should fail if there's not trace of VatFinancials in neither backend nor S4L" in new Setup {
      when(mockRegConnector.getRegistration(Matchers.eq(testRegId))(any(), any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNothing[S4LVatFinancials]()

      service.submitVatFinancials() failedWith classOf[IllegalStateException]
    }

  }
}
