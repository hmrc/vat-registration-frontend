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

import connectors.mocks.MockRegistrationApiConnector
import models.OtherBusinessInvolvement
import org.mockito.ArgumentMatchers.{any, eq => matches}
import org.mockito.Mockito.when
import services.OtherBusinessInvolvementsService._
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class OtherBusinessInvolvementsServiceSpec extends VatRegSpec with MockRegistrationApiConnector {

  val testBusinessName = "testBusinessName"
  val testVrn = "testVrn"
  val testUtr = "testUtr"
  val fullOtherBusinessInvolvement: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(true),
    vrn = Some(testVrn),
    hasUtr = None,
    utr = None,
    stillTrading = Some(true)
  )
  val fullOtherBusinessInvolvementWithUtr: OtherBusinessInvolvement = OtherBusinessInvolvement(
    businessName = Some(testBusinessName),
    hasVrn = Some(false),
    vrn = None,
    hasUtr = Some(true),
    utr = Some(testUtr),
    stillTrading = Some(true)
  )
  val idx = 1

  object TestService extends OtherBusinessInvolvementsService(
    mockS4LService,
    mockRegistrationApiConnector
  )

  "getOtherBusinessInvolvement" must {
    "return data from S4L" in {
      when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
        .thenReturn(Future.successful(Some(fullOtherBusinessInvolvement)))

      await(TestService.getOtherBusinessInvolvement(idx)) mustBe Some(fullOtherBusinessInvolvement)
    }

    "return data from BE" in {
      when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
        .thenReturn(Future.successful(None))
      mockGetSection(testRegId, Some(fullOtherBusinessInvolvement))

      await(TestService.getOtherBusinessInvolvement(idx)) mustBe Some(fullOtherBusinessInvolvement)
    }
  }

  "getOtherBusinessInvolvements" must {
    "return the list of OBI from BE" in {
      mockGetListSection(testRegId, List(fullOtherBusinessInvolvement))

      await(TestService.getOtherBusinessInvolvements) mustBe List(fullOtherBusinessInvolvement)
    }
  }

  "getHighestValidIndex" must {
    "return the highest valid index for this list" in {
      mockGetListSection(testRegId, List(fullOtherBusinessInvolvement, fullOtherBusinessInvolvement))

      await(TestService.getHighestValidIndex) mustBe 3
    }
  }

  "updateOtherBusinessInvolvement" must {
    "return OtherBusinessInvolvement if storing otherBusinessName" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(businessName = Some(testBusinessName))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, BusinessNameAnswer(testBusinessName))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvement)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvement)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, BusinessNameAnswer(testBusinessName)))
      }
    }

    "return OtherBusinessInvolvement if storing hasVrn" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(hasVrn = Some(true))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, HasVrnAnswer(true))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvement)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvement)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, HasVrnAnswer(true))) mustBe fullOtherBusinessInvolvement
      }
    }

    "return OtherBusinessInvolvement if storing vrn" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(vrn = Some(testVrn))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, VrnAnswer(testVrn))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvement)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvement)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, VrnAnswer(testVrn))) mustBe fullOtherBusinessInvolvement
      }
    }

    "return OtherBusinessInvolvement if storing hasUtr" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(hasUtr = Some(true))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, HasUtrAnswer(true))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvementWithUtr)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvementWithUtr)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, HasUtrAnswer(true))) mustBe fullOtherBusinessInvolvementWithUtr
      }
    }

    "return OtherBusinessInvolvement if storing utr" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(utr = Some(testUtr))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, UtrAnswer(testUtr))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvementWithUtr)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvementWithUtr)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, UtrAnswer(testUtr))) mustBe fullOtherBusinessInvolvementWithUtr
      }
    }

    "return OtherBusinessInvolvement if storing stillTrading" when {
      "the model is incomplete" in {
        val incompleteModel = OtherBusinessInvolvement(stillTrading = Some(true))
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(None))
        mockGetSection[OtherBusinessInvolvement](testRegId, None)
        when(mockS4LService.save[OtherBusinessInvolvement](matches(incompleteModel))(any(), any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, StillTradingAnswer(true))) mustBe incompleteModel
      }

      "the model is complete" in {
        when(mockS4LService.fetchAndGet[OtherBusinessInvolvement](any(), any(), any(), any()))
          .thenReturn(Future.successful(Some(fullOtherBusinessInvolvement)))
        mockReplaceSection[OtherBusinessInvolvement](testRegId, fullOtherBusinessInvolvement)
        when(mockS4LService.clearKey[OtherBusinessInvolvement](any(), any(), any()))
          .thenReturn(Future.successful(CacheMap("", Map())))

        await(TestService.updateOtherBusinessInvolvement(idx, StillTradingAnswer(true))) mustBe fullOtherBusinessInvolvement
      }
    }
  }
}
