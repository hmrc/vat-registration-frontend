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

import _root_.models._
import featureswitch.core.config.FeatureSwitching
import models.api.returns._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.JsString
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HttpResponse, InternalServerException, NotFoundException}

import java.time.LocalDate
import scala.concurrent.Future

class ReturnsServiceSpec extends VatRegSpec with FeatureSwitching {

  class Setup {
    val service = new ReturnsService(
      mockVatRegistrationConnector,
      mockVatRegistrationService,
      mockS4LService
    )
  }

  val mockCacheMap: CacheMap = CacheMap("", Map("" -> JsString("")))

  override val date: LocalDate = LocalDate.now
  override val returns: Returns = Returns(Some(testTurnover), None, Some(10000.5), Some(true), Some(Quarterly), Some(FebruaryStagger), Some(date))
  val returnsFixed: Returns = returns.copy(startDate = Some(LocalDate.of(2017, 12, 25)))
  val returnsAlt: Returns = returns.copy(startDate = Some(LocalDate.of(2017, 12, 12)))
  val testAASDetails: AASDetails = AASDetails(Some(MonthlyPayment), Some(BankGIRO))
  val testAASReturns: Returns = Returns(Some(testTurnover), None, Some(10000.5), Some(true), Some(Annual), Some(JanDecStagger), Some(date), Some(testAASDetails))

  def returnsWithVatDate(vd: Option[LocalDate]): Returns = returns.copy(startDate = vd)

  val emptyReturns: Returns = Returns(None, None, None, None, None, None)
  val incomplete: Returns = emptyReturns.copy(reclaimVatOnMostReturns = Some(true))
  val incompleteNIP: Returns = Returns(Some(testTurnover), None, Some(10000.5), Some(true), Some(Quarterly), Some(FebruaryStagger), Some(date), None, None, Some(NIPCompliance(Some(ConditionalValue(false, None)), None)))

  "getReturnsViewModel" should {
    "return a model from Save4Later" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))

      await(service.getReturns) mustBe returns
    }

    "return a model from MongoDB" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(returns))

      await(service.getReturns) mustBe returns
    }

    "construct a blank model when nothing was found in Save4Later or Mongo" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.failed(new NotFoundException("404")))

      await(service.getReturns) mustBe emptyReturns
    }
    "construct a blank model when an exception occurred fetching from Save4Later or Mongo" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.failed(new RuntimeException("BOOM")))

      await(service.getReturns) mustBe emptyReturns
    }
  }

  "handleView" should {
    "return a Complete frequency is decided for the user" in new Setup {
      val defaultedReturns: Returns = returns.copy(reclaimVatOnMostReturns = Some(false))
      service.handleView(defaultedReturns) mustBe Complete(defaultedReturns)
    }
    "return a Complete model when maximum data is provided" in new Setup {
      service.handleView(returns) mustBe Complete(returns)
    }
    "return a Complete model when minimum data is provided" in new Setup {
      val minReturns: Returns = returns.copy(returnsFrequency = Some(Monthly), staggerStart = None)
      service.handleView(minReturns) mustBe Complete(minReturns.copy(staggerStart = Some(MonthlyStagger)))
    }
    "return an Incomplete model when less than minimum data is provided" in new Setup {
      service.handleView(emptyReturns) mustBe Incomplete(emptyReturns)
    }
    "return an Incomplete model when NIP Compliance isn't complete but the rest of returns is provided" in new Setup {
      service.handleView(incompleteNIP) mustBe Incomplete(incompleteNIP)
    }
  }

  "submitReturns" should {
    "save a complete model to S4L" in new Setup {
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.submitReturns(returns)) mustBe returns
    }
    "save an incomplete model to S4L" in new Setup {
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.submitReturns(incomplete)) mustBe incomplete
    }
  }

  "getTurnover" should {
    "return turnover from Returns if it exists" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(Some(validTurnoverEstimates)))

      await(service.getTurnover) mustBe Some(testTurnover)
    }

    "fallback to turnover from EligibilitySubmissionData if Returns turnover doesn't exist" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns.copy(turnoverEstimate = None))))
      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(Some(validTurnoverEstimates)))

      await(service.getTurnover) mustBe Some(validTurnoverEstimates.turnoverEstimate)
    }
  }

  "saveTurnover" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveTurnover(turnoverEstimate = testTurnover)) mustBe returns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = emptyReturns.copy(
        turnoverEstimate = Some(testTurnover)
      )

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyReturns)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveTurnover(turnoverEstimate = testTurnover)) mustBe expected
    }
  }

  "saveVatExemption" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveVatExemption(appliedForExemption = true)) mustBe returns.copy(appliedForExemption = Some(true))
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = emptyReturns.copy(
        appliedForExemption = Some(true)
      )

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyReturns)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVatExemption(appliedForExemption = true)) mustBe expected
    }
  }

  "saveZeroRatesSupplies" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveZeroRatesSupplies(zeroRatedSupplies = 10000.5)) mustBe returns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = emptyReturns.copy(
        zeroRatedSupplies = Some(10000.5)
      )

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyReturns)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveZeroRatesSupplies(zeroRatedSupplies = 10000.5)) mustBe expected
    }
  }

  "saveReclaimVATOnMostReturns" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveReclaimVATOnMostReturns(reclaimView = true)) mustBe returns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = incomplete.copy(
        reclaimVatOnMostReturns = Some(false)
      )

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveReclaimVATOnMostReturns(reclaimView = false)) mustBe expected
    }
  }

  "saveFrequency" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveFrequency(Quarterly)) mustBe returns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = emptyReturns.copy(returnsFrequency = Some(Monthly))

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyReturns)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveFrequency(Monthly)) mustBe expected
    }
  }

  "saveStaggerStart" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveStaggerStart(FebruaryStagger)) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected: Returns = incomplete.copy(staggerStart = Some(JanuaryStagger))

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveStaggerStart(JanuaryStagger)) mustBe expected
    }
  }

  "saveVatStartDate" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.saveVatStartDate(Some(date))) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected: Returns = incomplete.copy(startDate = Some(date))

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVatStartDate(Some(date))) mustBe expected
    }
  }

  "retrieveCalculatedStartDate" should {
    "return the calculated date from EligibilitySubmissionData" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(testDate))))

      await(service.retrieveCalculatedStartDate) mustBe testDate
    }

    "throw a InternalServerException when calculated date is missing" in new Setup {
      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = None)))

      intercept[InternalServerException](await(service.retrieveCalculatedStartDate)).message mustBe "[ReturnsService] Missing calculated date"
    }
  }

  "retrieveMandatoryDates" should {
    val calculatedDate: LocalDate = LocalDate.of(2017, 12, 25)

    "return a full MandatoryDateModel with a selection of calculated_date if the vatStartDate is present and is equal to the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 25)

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsFixed)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.calculated_date))
    }

    "return a full MandatoryDateModel with a selection of specific_date if the vatStartDate does not equal the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 12)

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsAlt)))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.specific_date))
    }

    "return a MandatoryDateModel with just a calculated date if the vatStartDate is not present" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(calculatedDate = Some(calculatedDate))))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, None, None)
    }
  }

  "saveVoluntaryStartDate" should {
    "save a company start date as the vat start date" in new Setup {
      val expected: Returns = incomplete.copy(startDate = Some(testIncorpDate))

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.company_registration_date, None, testIncorpDate
      )) mustBe expected
    }

    "save a specific start date" in new Setup {
      val specificStartDate: LocalDate = LocalDate.of(2017, 12, 12)
      val expected: Returns = incomplete.copy(startDate = Some(specificStartDate))

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.specific_date, Some(specificStartDate), testIncorpDate
      )) mustBe expected
    }
  }

  "savePaymentFrequency" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(testAASReturns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.savePaymentFrequency(MonthlyPayment)) mustBe testAASReturns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = incomplete.copy(
        annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment)))
      )

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.savePaymentFrequency(MonthlyPayment)) mustBe expected
    }
  }

  "savePaymentMethod" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(testAASReturns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200, "{}")))

      await(service.savePaymentMethod(BankGIRO)) mustBe testAASReturns
    }

    "save an incomplete model" in new Setup {
      val expected: Returns = incomplete.copy(
        annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment), Some(BankGIRO)))
      )
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete.copy(annualAccountingDetails = Some(AASDetails(Some(MonthlyPayment)))))))
      when(mockS4LService.save(any)(any, any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.savePaymentMethod(BankGIRO)) mustBe expected
    }
  }

  "isEligibleForAAS" should {
    val validTurnover = 1350000L
    val invalidTurnover = 1350001L

    "return false for a turnover that is above 1350000" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns.copy(turnoverEstimate = Some(invalidTurnover)))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.isEligibleForAAS) mustBe false
    }

    "return false for a Groups Registration" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns.copy(turnoverEstimate = Some(validTurnover)))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData.copy(registrationReason = GroupRegistration)))

      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.isEligibleForAAS) mustBe false
    }

    "return true when the turnover estimate is valid for AAS" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returns.copy(turnoverEstimate = Some(validTurnover)))))

      when(mockVatRegistrationService.getEligibilitySubmissionData(any(), any()))
        .thenReturn(Future.successful(validEligibilitySubmissionData))

      when(mockVatRegistrationService.fetchTurnoverEstimates(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.isEligibleForAAS) mustBe true
    }
  }
}
