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

package features.sicAndCompliance.controllers

import connectors.KeystoreConnect
import features.frs.services.FlatRateService
import features.sicAndCompliance.models.SicAndCompliance
import features.sicAndCompliance.services.SicAndComplianceService
import fixtures.VatRegistrationFixture
import helpers.{ControllerSpec, FutureAssertions, MockMessages}
import mocks.SicAndComplianceServiceMock
import models.ModelKeys.SIC_CODES_KEY
import models.api.SicCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.i18n.MessagesApi
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future

class SicAndComplianceControllerSpec extends ControllerSpec with FutureAwaits with FutureAssertions with DefaultAwaitTimeout
                                     with VatRegistrationFixture with MockMessages with SicAndComplianceServiceMock {

  trait Setup {
    val controller: SicAndComplianceController = new SicAndComplianceController {
      override val keystoreConnector: KeystoreConnect = mockKeystoreConnector
      override val sicAndCompService: SicAndComplianceService = mockSicAndComplianceService
      override val frsService: FlatRateService = mockFlatRateService
      val messagesApi: MessagesApi = mockMessagesAPI
      val authConnector: AuthConnector = mockAuthClientConnector
    }

    mockAllMessages
    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }


  val validLabourSicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance = SicCode("12345678", "fooBar", "FooBar")

  s"GET ${routes.SicAndComplianceController.showBusinessActivityDescription()}" should {
    "return HTML Business Activity Description page with no data in the form" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      callAuthorised(controller.showBusinessActivityDescription) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(SicAndCompliance()))

      callAuthorised(controller.showBusinessActivityDescription) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitBusinessActivityDescription()} with Empty data" should {
    val fakeRequest = FakeRequest(routes.SicAndComplianceController.showBusinessActivityDescription())

    "return 400" in new Setup {
      submitAuthorised(controller.submitBusinessActivityDescription(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(controller.submitBusinessActivityDescription(), fakeRequest.withFormUrlEncodedBody("description" -> "Testing")) {
        _ redirectsTo "/sic-stub"
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showComplianceIntro()}" should {
    "display the introduction page to a set of compliance questions" in new Setup {
      callAuthorised(controller.showComplianceIntro) {
        _ includesText MOCKED_MESSAGE
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitComplianceIntro()}" should {
    "redirect the user to the SIC code selection page" in new Setup {
      callAuthorised(controller.submitComplianceIntro) {
        result =>
          result redirectsTo routes.LabourComplianceController.showProvideWorkers().url
      }
    }
  }

  s"GET ${routes.SicAndComplianceController.showMainBusinessActivity()}" should {
    "return HTML when view present in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity()) {
        _ includesText MOCKED_MESSAGE
      }
    }

    "return HTML where getSicAndCompliance returns empty viewModels for labour" in new Setup {
      mockGetSicAndCompliance(Future.successful(SicAndCompliance()))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(controller.showMainBusinessActivity) { result =>
        result includesText MOCKED_MESSAGE
        status(result) mustBe 200
      }
    }
  }

  s"POST ${routes.SicAndComplianceController.submitMainBusinessActivity()}" should {
    val fakeRequest = FakeRequest(routes.SicAndComplianceController.showMainBusinessActivity())

    "return 400" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      submitAuthorised(controller.submitMainBusinessActivity(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))


      when(mockFlatRateService.resetFRS(any())(any(), any()))
        .thenReturn(Future.successful(sicCode))

      when(mockSicAndComplianceService.needComplianceQuestions(any()))
        .thenReturn(true)

      submitAuthorised(controller.submitMainBusinessActivity(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validLabourSicCode.id)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
    "return 303 with selected sicCode (noCompliance) and sicCode list in keystore" in new Setup {

      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validNoCompliance)))

      when(mockSicAndComplianceService.updateSicAndCompliance(any())(any(), any()))
        .thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))


      when(mockFlatRateService.resetFRS(any())(any(), any()))
        .thenReturn(Future.successful(sicCode))

      when(mockSicAndComplianceService.needComplianceQuestions(any()))
        .thenReturn(false)

      submitAuthorised(controller.submitMainBusinessActivity(),

        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validNoCompliance.id)
      )(_ redirectsTo s"$contextRoot/trading-name")
    }
  }
}
