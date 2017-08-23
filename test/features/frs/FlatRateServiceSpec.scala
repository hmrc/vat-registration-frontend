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

package services {

  import cats.data.OptionT
  import connectors.KeystoreConnector
  import fixtures.VatRegistrationFixture
  import helpers.{S4LMockSugar, VatRegSpec}
  import models.S4LFlatRateScheme
  import models.external.IncorporationInfo
  import models.view.frs._
  import org.mockito.Matchers
  import org.mockito.Matchers.any
  import org.mockito.Mockito._

  import scala.concurrent.Future
  import scala.language.postfixOps

  class FlateRateServiceSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {
    class Setup {
      val service: FlatRateService = new RegistrationService {
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
      when(mockIIService.getIncorporationInfo()(any())).thenReturn(OptionT.none[Future, IncorporationInfo])
    }

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
  }
}
