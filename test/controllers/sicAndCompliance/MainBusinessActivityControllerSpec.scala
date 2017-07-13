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
import models.api.SicCode
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

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

    "return HTML when there's nothing in S4L and vatScheme contains empty data" in {
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(Controller.show()) {
        _ includesText "Which business activity is the company"
      }
    }


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)
      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(Controller.show()) {
        _ includesText "Which business activity is the company"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = None)

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)
      save4laterReturnsNoViewModel[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(Controller.show()) {
        _ includesText "Which business activity is the company"
      }
    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()} with Empty data" should {

    "return 400" in {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()} with selected sicCode but no sicCode list in keystore" should {

    "return 400" in {
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ isA 400)

    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()} with selected sicCode" should {

    "return 303" in {
      save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(sicCode)))
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(validSicAndCompliance.pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ redirectsTo s"$contextRoot/business-bank-account")

    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()} with selected sicCode and sicCode list in keystore" should {

    "return 303" in {
      save4laterReturnsViewModel(MainBusinessActivityView(sicCode))()
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validSicCode)))
      when(mockS4LService.save(any())(any(), any(), any())).thenReturn(dummyCacheMap.pure)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(validSicAndCompliance.pure)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validSicCode.id)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
  }

  s"POST ${routes.MainBusinessActivityController.redirectToNext()}  sicCode list in keystore and redirect to redirectToNext method" should {

    "return 303" in {
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validSicCode)))
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)

      callAuthorised(Controller.redirectToNext())(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")


    }
  }

  s"POST ${routes.MainBusinessActivityController.redirectToNext()}  Empty Sic Code list in keystore and redirect to redirectToNext method" should {

    "return 303" in {
      save4laterExpectsSave[MainBusinessActivityView]()
      mockKeystoreFetchAndGet(SIC_CODES_KEY, None)
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockVatRegistrationService.submitSicAndCompliance()(any())).thenReturn(validSicAndCompliance.pure)
      when(mockVatRegistrationService.deleteElements(any())(any())).thenReturn(().pure)

      callAuthorised(Controller.redirectToNext())(_ redirectsTo s"$contextRoot/business-bank-account")
    }
  }

}
