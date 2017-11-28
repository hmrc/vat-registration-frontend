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

package controllers.frs

import connectors.ConfigConnector
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, MockMessages}
import models.view.frs.BusinessSectorView
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import services.VatRegistrationService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class BusinessSectorAwareControllerSpec extends ControllerSpec with VatRegistrationFixture with MockMessages {

  val testBusinessSectorView = BusinessSectorView("test business sector", 4.33)
  val limitedCostCompanyRate = BusinessSectorView("", 16.5)

  trait Setup {
    val controller: BusinessSectorAwareController = new BusinessSectorAwareController {
      override val service: VatRegistrationService = mockVatRegistrationService
      override val configConnect: ConfigConnector = mockConfigConnector
      override val messagesApi: MessagesApi = mockMessagesAPI
      override val authConnector: AuthConnector = mockAuthConnector
    }

    mockAllMessages
  }

  "BusinessSectorAwareController" should {

    val s4LFlatRateSchemeNoBusinessSector = validS4LFlatRateScheme.copy(categoryOfBusiness = None)
    val s4LFlatRateSchemeBusinessSectorIsBlank = validS4LFlatRateScheme.copy(categoryOfBusiness = Some(BusinessSectorView("", 1)))

    val s4LVatSicAndComplianceNoMainBusinessActivity = s4LVatSicAndCompliance.copy(mainBusinessActivity = None)

    "retrieve a businessSectorView if one is saved" in new Setup {
      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(validS4LFlatRateScheme))

      await(controller.businessSectorView()) mustBe validBusinessSectorView
    }

    "determine a businessSectorView if none is saved but main business activity is known" in new Setup {
      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(s4LFlatRateSchemeNoBusinessSector))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndCompliance))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(controller.businessSectorView()) mustBe validBusinessSectorView
    }

    "determine a businessSectorView if business sector is blank but main business activity is known" in new Setup {
      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(s4LFlatRateSchemeBusinessSectorIsBlank))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndCompliance))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      await(controller.businessSectorView()) mustBe validBusinessSectorView
    }

    "fail if no BusinessSectorView is saved and main business activity is not known" in new Setup {
      when(mockVatRegistrationService.fetchFlatRateScheme(any(), any()))
        .thenReturn(Future.successful(s4LFlatRateSchemeNoBusinessSector))

      when(mockVatRegistrationService.fetchSicAndCompliance(any(), any()))
        .thenReturn(Future.successful(s4LVatSicAndComplianceNoMainBusinessActivity))

      when(mockConfigConnector.getBusinessSectorDetails(any()))
        .thenReturn(validBusinessSectorView)

      val exception: IllegalStateException = intercept[IllegalStateException](await(controller.businessSectorView()))

      exception.getMessage mustBe "Can't determine main business activity"
    }
  }
}
