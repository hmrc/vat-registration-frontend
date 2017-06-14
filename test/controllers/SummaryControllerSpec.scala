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

package controllers

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.{VatScheme, VatSicAndCompliance}
import models.view.Summary
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class SummaryControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object TestSummaryController extends SummaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector

    override def getRegistrationSummary()(implicit hc: HeaderCarrier): Future[Summary] = Summary(sections = Seq()).pure
  }

  object TestSummaryController2 extends SummaryController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  "Correct compliance section should be rendered" when {

    "labour questions have been answered by user" in {
      val vs = VatScheme("ID", vatSicAndCompliance = Some(VatSicAndCompliance("TEST", labourCompliance = Some(validVatLabourCompliance))))
      val summarySection = TestSummaryController.complianceSection(vs)
      summarySection.id mustBe "labourCompliance"
    }

    "cultural questions have been answered by user" in {
      val vs = VatScheme("ID", vatSicAndCompliance = Some(VatSicAndCompliance("TEST", culturalCompliance = Some(validVatCulturalCompliance))))
      val summarySection = TestSummaryController.complianceSection(vs)
      summarySection.id mustBe "culturalCompliance"
    }

    "financial questions have been answered by user" in {
      val vs = VatScheme("ID", vatSicAndCompliance = Some(VatSicAndCompliance("TEST", financialCompliance = Some(validVatFinancialCompliance))))
      val summarySection = TestSummaryController.complianceSection(vs)
      summarySection.id mustBe "financialCompliance"
    }


    "No compliance questions have been answered by user" in {
      val vs = VatScheme("ID", vatSicAndCompliance = None)
      assertThrows[IllegalStateException] {
        TestSummaryController.complianceSection(vs)
      }
    }

  }

  "Calling summary to show the summary page" should {
    "return HTML with a valid summary view" in {
      when(mockVatRegistrationService.submitVatScheme()(any())).thenReturn(().pure)
      when(mockS4LService.clear()(any())).thenReturn(validHttpResponse.pure)

      callAuthorised(TestSummaryController.show)(_ includesText "Check and confirm your answers")
    }

    "getRegistrationSummary maps a valid VatScheme object to a Summary object" in {
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)
      TestSummaryController2.getRegistrationSummary().map(summary => summary.sections.length mustEqual 2)
    }

    "registrationToSummary maps a valid VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(validVatScheme).sections.length mustEqual 11
    }

    "registrationToSummary maps a valid empty VatScheme object to a Summary object" in {
      TestSummaryController.registrationToSummary(emptyVatSchemeWithAccountingPeriodFrequency).sections.length mustEqual 11
    }

  }

}
