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

import _root_.models._
import _root_.models.api.Threshold
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.MustMatchers
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsString
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HttpResponse, NotFoundException}

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

  val mockCacheMap = CacheMap("", Map("" -> JsString("")))

  override val date = LocalDate.now
  override val returns = Returns(Some(true), Some(Frequency.quarterly), Some(Stagger.feb), Some(Start(Some(date))))
  val returnsFixed = returns.copy(start = Some(Start(Some(LocalDate.of(2017, 12, 25)))))
  val returnsAlt = returns.copy(start = Some(Start(Some(LocalDate.of(2017, 12, 12)))))

  def returnsWithVatDate(vd: Option[LocalDate]) = returns.copy(start = Some(Start(vd)))

  val emptyReturns = Returns(None, None, None, None)
  val incomplete = emptyReturns.copy(reclaimVatOnMostReturns = Some(true))

  "getReturnsViewModel" should {
    "return a model from Save4Later" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))

      await(service.getReturns) mustBe returns
    }

    "return a model from MongoDB" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(returns))

      await(service.getReturns) mustBe returns
    }

    "construct a blank model when nothing was found in Save4Later or Mongo" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockVatRegistrationConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.failed(new NotFoundException("404")))

      await(service.getReturns) mustBe emptyReturns
    }
    "construct a blank model when an exception occurred fetching from Save4Later or Mongo" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.failed(new RuntimeException("BOOM")))

      await(service.getReturns) mustBe emptyReturns
    }
  }

  "handleView" should {
    "should return a Complete frequency is decided for the user" in new Setup {
      val defaultedReturns = returns.copy(reclaimVatOnMostReturns = Some(false))
      service.handleView(defaultedReturns) mustBe Complete(defaultedReturns)
    }
    "should return a Complete model when maximum data is provided" in new Setup {
      service.handleView(returns) mustBe Complete(returns)
    }
    "should return a Complete model when minimum data is provided" in new Setup {
      val minReturns = returns.copy(frequency = Some(Frequency.monthly), staggerStart = None)
      service.handleView(minReturns) mustBe Complete(minReturns)
    }
    "should return an Incomplete model when less than minimum data is provided" in new Setup {
      service.handleView(emptyReturns) mustBe Incomplete(emptyReturns)
    }
  }

  "submitReturns" should {
    "save a complete model to S4L" in new Setup {
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.submitReturns(returns)) mustBe returns
    }
    "save an incomplete model to S4L" in new Setup {
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.submitReturns(incomplete)) mustBe incomplete
    }
  }

  "saveReclaimVATOnMostReturns" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveReclaimVATOnMostReturns(reclaimView = true)) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected = incomplete.copy(
        reclaimVatOnMostReturns = Some(false),
        frequency = Some(Frequency.quarterly)
      )

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveReclaimVATOnMostReturns(reclaimView = false)) mustBe expected
    }
  }

  "saveFrequency" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveFrequency(Frequency.quarterly)) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected = emptyReturns.copy(frequency = Some(Frequency.monthly))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(emptyReturns)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveFrequency(Frequency.monthly)) mustBe expected
    }
  }

  "saveStaggerStart" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveStaggerStart(Stagger.feb)) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected = incomplete.copy(staggerStart = Some(Stagger.jan))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveStaggerStart(Stagger.jan)) mustBe expected
    }
  }

  "saveVatStartDate" should {
    "save a complete model" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returns)))
      when(mockVatRegistrationConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveVatStartDate(Some(date))) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected = incomplete.copy(start = Some(Start(Some(date))))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVatStartDate(Some(date))) mustBe expected
    }
  }

  "retrieveCalculatedStartDate" should {

    val overTwelveMonthDate = LocalDate.of(2017, 6, 6)
    val pastThirtyDayDate = LocalDate.of(2017, 12, 12)

    "return a date when both the vatThresholdPostIncorp and vatExpectedThresholdPostIncorp dates are present" in new Setup {

      val thresholdDates = Threshold(
        true,
        Some(pastThirtyDayDate),
        Some(overTwelveMonthDate)
      )


      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(thresholdDates))

      await(service.retrieveCalculatedStartDate) mustBe overTwelveMonthDate.withDayOfMonth(1).plusMonths(2)
    }

    "return a date when just the overTwelveMonthDate is present" in new Setup {

      val thresholdFirstDateOnly = Threshold(true, None, Some(overTwelveMonthDate))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(thresholdFirstDateOnly))

      await(service.retrieveCalculatedStartDate) mustBe overTwelveMonthDate.withDayOfMonth(1).plusMonths(2)
    }

    "return a date when just the pastThirtyDayDate is present" in new Setup {

      val thresholdSecondDateOnly = generateThreshold(thresholdPreviousThirtyDays = Some(pastThirtyDayDate))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(thresholdSecondDateOnly))

      await(service.retrieveCalculatedStartDate) mustBe pastThirtyDayDate
    }

    "throw a RuntimeException when no dates are present" in new Setup {

      val thresholdNoDates = generateThreshold()

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(thresholdNoDates))

      intercept[RuntimeException](await(service.retrieveCalculatedStartDate))
    }

    "return a RuntimeException when the eligibilityChoice section of the scheme is not present" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      intercept[RuntimeException](await(service.retrieveCalculatedStartDate))
    }
  }

  "getThreshold" should {
    "return true when in a voluntary flow" in new Setup {

      val voluntary = generateThreshold()

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(voluntary))

      await(service.getThreshold) mustBe true

    }

    "return false when in a mandatory flow" in new Setup {
      val mandatory = generateThreshold(thresholdPreviousThirtyDays = Some(testDate))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(mandatory))

      await(service.getThreshold) mustBe false
    }
  }

  "retrieveMandatoryDates" should {
    val calculatedDate: LocalDate = LocalDate.of(2017, 12, 25)

    val threshold = generateThreshold(thresholdPreviousThirtyDays = Some(calculatedDate))

    "return a full MandatoryDateModel with a selection of calculated_date if the vatStartDate is present and is equal to the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 25)

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsFixed)))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(threshold))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.calculated_date))
    }

    "return a full MandatoryDateModel with a selection of specific_date if the vatStartDate does not equal the calculated date" in new Setup {
      val vatStartDate: LocalDate = LocalDate.of(2017, 12, 12)

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsAlt)))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(threshold))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, Some(vatStartDate), Some(DateSelection.specific_date))
    }

    "return a MandatoryDateModel with just a calculated date if the vatStartDate is not present" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(service.vatService.getThreshold(any())(any()))
        .thenReturn(Future.successful(threshold))

      await(service.retrieveMandatoryDates) mustBe MandatoryDateModel(calculatedDate, None, None)
    }
  }

  "retrieveCTActiveDate" should {
    "return the CT Active Date" in new Setup {
      when(mockPrePopulationService.getCTActiveDate(any(), any()))
        .thenReturn(Future.successful(Some(date)))

      await(service.retrieveCTActiveDate) mustBe Some(date)
    }
  }

  "getVatStartDate" should {
    "return the vat start if it exists" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsFixed)))

      await(service.getVatStartDate) mustBe Some(LocalDate.of(2017, 12, 25))
    }

    "return nothing if it doesn't exist" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      await(service.getVatStartDate) mustBe None
    }
  }

  "voluntaryStartPageViewModel" should {
    "return a business start date view model" in new Setup {
      val businessStartDate = LocalDate.of(2017, 10, 10)

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsWithVatDate(Some(businessStartDate)))))

      when(mockPrePopulationService.getCTActiveDate(any(), any()))
        .thenReturn(Future.successful(Some(businessStartDate)))

      await(service.voluntaryStartPageViewModel()) mustBe VoluntaryPageViewModel(
        Some((DateSelection.business_start_date, Some(businessStartDate))),
        Some(businessStartDate)
      )
    }

    "return a future incorp date view model when the company is not incorped" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsWithVatDate(None))))

      when(mockPrePopulationService.getCTActiveDate(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.voluntaryStartPageViewModel()) mustBe VoluntaryPageViewModel(
        Some((DateSelection.company_registration_date, None)),
        None
      )
    }

    "return a specific date when it is selected" in new Setup {
      val specificdate = LocalDate.of(2017, 12, 12)

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(returnsWithVatDate(Some(specificdate)))))

      when(mockPrePopulationService.getCTActiveDate(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.voluntaryStartPageViewModel()) mustBe VoluntaryPageViewModel(
        Some((DateSelection.specific_date, Some(specificdate))),
        None
      )
    }

    "return an empty view model if nothing has been previously selected" in new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      when(mockPrePopulationService.getCTActiveDate(any(), any()))
        .thenReturn(Future.successful(None))

      await(service.voluntaryStartPageViewModel()) mustBe VoluntaryPageViewModel(None, None)
    }
  }

  "saveVoluntaryStartDate" should {

    "save a company registration date when the incorp date in the future, to be incorped" in new Setup {
      val expected = incomplete.copy(start = Some(Start(None)))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.company_registration_date, None, None
      )) mustBe expected
    }

    "save a business start date as the vat start date" in new Setup {
      val businessStartDate = LocalDate.of(2017, 11, 11)
      val expected = incomplete.copy(start = Some(Start(Some(businessStartDate))))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.business_start_date, None, Some(businessStartDate)
      )) mustBe expected
    }

    "save a specific start date" in new Setup {
      val specificStartDate = LocalDate.of(2017, 12, 12)
      val expected = incomplete.copy(start = Some(Start(Some(specificStartDate))))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.specific_date, Some(specificStartDate), None
      )) mustBe expected
    }

    "save an empty start if no start date is provided" in new Setup {
      val expected = incomplete.copy(start = Some(Start(None)))

      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(Some(incomplete)))
      when(mockS4LService.saveNoAux(any, any)(any, any, any))
        .thenReturn(Future.successful(mockCacheMap))

      await(service.saveVoluntaryStartDate(
        DateSelection.specific_date, None, None
      )) mustBe expected
    }
  }
}
