/*
 * Copyright 2021 HM Revenue & Customs
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
import _root_.models.api.{NETP, Threshold}
import models.api.returns._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import play.api.libs.json.JsString
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HttpResponse, InternalServerException, NotFoundException}

import java.time.LocalDate
import scala.concurrent.Future

class ReturnsServiceSpec extends VatRegSpec with MustMatchers {

  class Setup {
    val service = new ReturnsService(
      mockVatRegistrationConnector,
      mockVatRegistrationService,
      mockS4LService,
      mockPrePopulationService
    )
  }

  val mockCacheMap: CacheMap = CacheMap("", Map("" -> JsString("")))

  override val date: LocalDate = LocalDate.now
  override val returns: Returns = Returns(Some(10000.5), Some(true), Some(Quarterly), Some(FebruaryStagger), Some(date))
  val returnsFixed: Returns = returns.copy(startDate = Some(LocalDate.of(2017, 12, 25)))
  val returnsAlt: Returns = returns.copy(startDate = Some(LocalDate.of(2017, 12, 12)))
  val testAASDetails: AASDetails = AASDetails(Some(MonthlyPayment), Some(BankGIRO))
  val testAASReturns: Returns = Returns(Some(10000.5), Some(true), Some(Annual), Some(JanDecStagger), Some(date), Some(testAASDetails))

  def returnsWithVatDate(vd: Option[LocalDate]): Returns = returns.copy(startDate = vd)

  val emptyReturns: Returns = Returns(None, None, None, None, None, None)
  val incomplete: Returns = emptyReturns.copy(reclaimVatOnMostReturns = Some(true))

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

    val overTwelveMonthDate = LocalDate.of(2017, 6, 6)
    val pastThirtyDayDate = LocalDate.of(2017, 12, 12)
    val nextThirtyDayDate = LocalDate.of(2017, 12, 12)
    val overseasDate = LocalDate.now()

    "return a date when both the thresholdPreviousThirtyDays and thresholdInTwelveMonths dates are present" in new Setup {
      val thresholdDates: Threshold = Threshold(
        mandatoryRegistration = true,
        Some(pastThirtyDayDate),
        Some(overTwelveMonthDate)
      )
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdDates))
        ))
      )

      await(service.retrieveCalculatedStartDate) mustBe overTwelveMonthDate.withDayOfMonth(1).plusMonths(2)
    }

    "return a date when just the thresholdInTwelveMonths is present" in new Setup {
      val thresholdFirstDateOnly: Threshold = Threshold(mandatoryRegistration = true, None, Some(overTwelveMonthDate))
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdFirstDateOnly))
        ))
      )

      await(service.retrieveCalculatedStartDate) mustBe overTwelveMonthDate.withDayOfMonth(1).plusMonths(2)
    }

    "return a date when just the thresholdPreviousThirtyDays is present" in new Setup {
      val thresholdSecondDateOnly: Threshold = generateThreshold(thresholdPreviousThirtyDays = Some(pastThirtyDayDate))
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdSecondDateOnly))
        ))
      )

      await(service.retrieveCalculatedStartDate) mustBe pastThirtyDayDate
    }

    "return a date when just the thresholdNextThirtyDays is present" in new Setup {
      val thresholdSecondDateOnly: Threshold = Threshold(
        mandatoryRegistration = true,
        thresholdNextThirtyDays = Some(nextThirtyDayDate)
      )
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdSecondDateOnly))
        ))
      )

      await(service.retrieveCalculatedStartDate) mustBe nextThirtyDayDate
    }

    "return a date when thresholdOverseas is present for an overseas user" in new Setup {
      val thresholdSecondDateOnly: Threshold = Threshold(
        mandatoryRegistration = true,
        thresholdOverseas = Some(overseasDate)
      )
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdSecondDateOnly, partyType = NETP))
        ))
      )

      await(service.retrieveCalculatedStartDate) mustBe overseasDate
    }

    "throw a InternalServerException when no dates are present" in new Setup {
      val thresholdNoDates: Threshold = generateThreshold()
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdNoDates))
        ))
      )

      intercept[InternalServerException](await(service.retrieveCalculatedStartDate)).message mustBe "[ReturnsService] Unable to calculate start date due to missing threshold data"
    }

    "throw a InternalServerException when overseas date is missing for overseas user" in new Setup {
      val thresholdNoDates: Threshold = generateThreshold()
      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = thresholdNoDates, partyType = NETP))
        ))
      )

      intercept[InternalServerException](await(service.retrieveCalculatedStartDate)).message mustBe "[ReturnsService] Overseas user missing overseas threshold date"
    }
  }

  "getThreshold" should {
    "return true when in a voluntary flow" in new Setup {
      val voluntary: Threshold = generateThreshold()

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(voluntary))

      await(service.isVoluntary) mustBe true

    }

    "return false when in a mandatory flow" in new Setup {
      val mandatory: Threshold = generateThreshold(thresholdPreviousThirtyDays = Some(testDate))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(mandatory))

      await(service.isVoluntary) mustBe false
    }
  }

  "retrieveMandatoryDates" should {
    val calculatedDate: LocalDate = LocalDate.of(2017, 12, 25)

    val threshold = generateThreshold(thresholdPreviousThirtyDays = Some(calculatedDate))

    "return a full MandatoryDateModel with a selection of calculated_date if the vatStartDate is present and is equal to the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 25)

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsFixed)))

      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = threshold))
        ))
      )

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.calculated_date))
    }

    "return a full MandatoryDateModel with a selection of specific_date if the vatStartDate does not equal the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 12)

      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsAlt)))

      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = threshold))
        ))
      )

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.specific_date))
    }

    "return a MandatoryDateModel with just a calculated date if the vatStartDate is not present" in new Setup {
      when(mockS4LService.fetchAndGet[Returns](any[S4LKey[Returns]](), any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(service.vatService.getVatScheme(any(), any())).thenReturn(
        Future.successful(emptyVatScheme.copy(
          eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(threshold = threshold))
        ))
      )

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
}
