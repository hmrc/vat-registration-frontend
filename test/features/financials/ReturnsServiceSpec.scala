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

import connectors.RegistrationConnector
import features.financials.models.{Frequency, Returns, Stagger, Start}
import helpers.VatRegSpec
import models.S4LKey
import models.api.{VatEligibilityChoice, VatExpectedThresholdPostIncorp, VatServiceEligibility, VatThresholdPostIncorp}
import org.mockito.ArgumentMatchers.any
import org.scalatest.MustMatchers
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.JsString
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HttpResponse, NotFoundException}

import scala.concurrent.Future


class ReturnsServiceSpec extends VatRegSpec with MustMatchers with MockitoSugar {

  class Setup {
    val service = new ReturnsService {
      override val s4lService: S4LService = mockS4LService
      override val vatService: RegistrationService = mockVatRegistrationService
      override val vatRegConnector: RegistrationConnector = mockRegConnector
    }
  }

  val mockCacheMap = CacheMap("", Map("" -> JsString("")))

  val date         = LocalDate.now
  val returns      = Returns(Some(true), Some(Frequency.quarterly), Some(Stagger.feb), Some(Start(Some(date))))
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
      when(mockRegConnector.getReturns(any())(any(), any()))
        .thenReturn(Future.successful(returns))

      await(service.getReturns) mustBe returns
    }

    "construct a blank model when nothing was found in Save4Later or Mongo" in  new Setup {
      when(mockS4LService.fetchAndGetNoAux[Returns](any[S4LKey[Returns]]())(any(), any(), any()))
        .thenReturn(Future.successful(None))
      when(mockRegConnector.getReturns(any())(any(), any()))
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
      when(mockRegConnector.patchReturns(any(), any[Returns])(any(), any()))
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
      when(mockRegConnector.patchReturns(any(), any[Returns])(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))
      when(mockS4LService.clear(any(), any()))
        .thenReturn(Future.successful(HttpResponse(200)))

      await(service.saveReclaimVATOnMostReturns(reclaimView = true)) mustBe returns
    }
    "save an incomplete model" in new Setup {
      val expected = incomplete.copy(reclaimVatOnMostReturns = Some(false))

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
      when(mockRegConnector.patchReturns(any(), any[Returns])(any(), any()))
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
      when(mockRegConnector.patchReturns(any(), any[Returns])(any(), any()))
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
      when(mockRegConnector.patchReturns(any(), any[Returns])(any(), any()))
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

  "mandatoryStartDate" should {

    val vatThresholdPostIncorpDate = LocalDate.of(2017, 6, 6)
    val vatExpectedThresholdPostIncorpDate = LocalDate.of(2017, 12, 12)

    "return a date when both the vatThresholdPostIncorp and vatExpectedThresholdPostIncorp dates are present" in new Setup {

      val eligibilityBothDates = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(
          VatEligibilityChoice.NECESSITY_OBLIGATORY,
          None,
          Some(VatThresholdPostIncorp(true, Some(vatThresholdPostIncorpDate))),
          Some(VatExpectedThresholdPostIncorp(true, Some(vatExpectedThresholdPostIncorpDate)))
        ))
      )

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(eligibilityBothDates))))

      await(service.retrieveCalculatedStartDate) mustBe Some(vatThresholdPostIncorpDate.withDayOfMonth(1).plusMonths(2))
    }

    "return a date when just the vatThresholdPostIncorp is present" in new Setup {

      val eligibilityFirstDateOnly = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(
          VatEligibilityChoice.NECESSITY_OBLIGATORY,
          None,
          Some(VatThresholdPostIncorp(true, Some(vatThresholdPostIncorpDate))),
          None
        ))
      )

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(eligibilityFirstDateOnly))))

      await(service.retrieveCalculatedStartDate) mustBe Some(vatThresholdPostIncorpDate.withDayOfMonth(1).plusMonths(2))
    }

    "return a date when just the vatExpectedThresholdPostIncorp is present" in new Setup {

      val eligibilitySecondDateOnly = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(
          VatEligibilityChoice.NECESSITY_OBLIGATORY,
          None,
          None,
          Some(VatExpectedThresholdPostIncorp(true, Some(vatExpectedThresholdPostIncorpDate)))
        ))
      )

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(eligibilitySecondDateOnly))))

      await(service.retrieveCalculatedStartDate) mustBe Some(vatExpectedThresholdPostIncorpDate)
    }

    "return a None when no dates are present" in new Setup {

      val eligibilityNoDates = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(VatEligibilityChoice.NECESSITY_OBLIGATORY, None, None, None))
      )

      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(eligibilityNoDates))))

      await(service.retrieveCalculatedStartDate) mustBe None
    }

    "return a None when the eligibilityChoice section of the scheme is not present" in new Setup {
      when(mockVatRegistrationService.getVatScheme(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme))

      await(service.retrieveCalculatedStartDate) mustBe None
    }
  }

  "getEligibilityChoice" should {
    "return true when in a voluntary flow" in new Setup {

      val voluntary = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(VatEligibilityChoice.NECESSITY_VOLUNTARY, None, None, None))
      )

      when(service.vatRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(voluntary))))

      await(service.getEligibilityChoice) mustBe true

    }

    "return false when in a mandatory flow" in new Setup {
      val mandatory = VatServiceEligibility(None, None, None, None, None, None,
        Some(VatEligibilityChoice(VatEligibilityChoice.NECESSITY_OBLIGATORY, None, None, None))
      )

      when(service.vatRegConnector.getRegistration(any())(any(), any()))
        .thenReturn(Future.successful(emptyVatScheme.copy(vatServiceEligibility = Some(mandatory))))

      await(service.getEligibilityChoice) mustBe false
    }
  }
}
