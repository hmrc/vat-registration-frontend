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

package controllers.sicAndCompliance

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys._
import models.{CurrentProfile, S4LVatSicAndCompliance}
import models.api.SicCode
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

import scala.concurrent.Future

class MainBusinessActivityControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends MainBusinessActivityController(ds)(
    mockS4LService,
    mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val validSicCode = SicCode("70221001", "Accounting systems design", "Financial management")
  val fakeRequest = FakeRequest(controllers.sicAndCompliance.routes.MainBusinessActivityController.show())

  s"GET ${routes.MainBusinessActivityController.show()}" should {
    "return HTML when there's nothing in S4L" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      callAuthorised(Controller.show()) {
        _ includesText "Which business activity is the company"
      }
    }

    "return HTML when view present in S4L" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturnsViewModel(MainBusinessActivityView(sicCode.id, Some(sicCode)))()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      callAuthorised(Controller.show()) {
        _ includesText "Which business activity is the company"
      }
    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()}" should {
    "return 400" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(sicCode)))
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(validSicAndCompliance.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")

    }

    "return 303 with selected sicCode and sicCode list in keystore" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validSicCode)))
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(validSicAndCompliance.pure)
      when(mockVatRegistrationService.submitVatFlatRateScheme()(any(), any())).thenReturn(validVatFlatRateScheme.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validSicCode.id)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
  }

  s"POST ${routes.MainBusinessActivityController.redirectToNext()}" should {
    "return 303 sicCode list in keystore and redirect to redirectToNext method" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validSicCode)))
      when(mockVatRegistrationService.getVatScheme()(any(), any())).thenReturn(emptyVatScheme.pure)
      callAuthorised(Controller.redirectToNext())(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")
    }

    "return 303 Empty Sic Code list in keystore and redirect to redirectToNext method" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, None)
      when(mockVatRegistrationService.submitSicAndCompliance()(any(), any())).thenReturn(validSicAndCompliance.pure)
      when(mockS4LService.save(any())(any(), any(), any(), any())).thenReturn(dummyCacheMap.pure)
      save4laterReturns(S4LVatSicAndCompliance())
      callAuthorised(Controller.redirectToNext())(_ redirectsTo s"$contextRoot/trade-goods-services-with-countries-outside-uk")
    }
  }
}