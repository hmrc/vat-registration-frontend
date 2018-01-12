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

package controllers.sicAndCompliance

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.ModelKeys._
import models.S4LVatSicAndCompliance
import models.api.SicCode
import models.view.sicAndCompliance.MainBusinessActivityView
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

import scala.concurrent.Future

class MainBusinessActivityControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  trait Setup {
    object Controller extends MainBusinessActivityController(
      ds,
      mockKeystoreConnector,
      mockAuthConnector,
      mockS4LService,
      mockSicAndComplianceSrv,
      mockVatRegistrationService
    )

    mockGetCurrentProfile()
  }


  val validLabourSicCode = SicCode("81221001", "BarFoo", "BarFoo")
  val validNoCompliance = SicCode("12345678", "fooBar", "FooBar")
  val fakeRequest = FakeRequest(controllers.sicAndCompliance.routes.MainBusinessActivityController.show())

  s"GET ${routes.MainBusinessActivityController.show()}" should {
    "return HTML when view present in S4L" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      callAuthorised(Controller.show()) {
        _ includesText "Which activity is the company&#x27;s main source of income?"
      }
    }
  }

  s"POST ${routes.MainBusinessActivityController.submit()}" should {
    "return 400" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, None)

      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody()
      )(result => result isA 400)
    }

    "return 400 with selected sicCode but no sicCode list in keystore" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Option.empty[List[SicCode]])

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> sicCode.id)
      )(_ isA 400)

    }

    "return 303 with selected sicCode" in new Setup {
      mockKeystoreFetchAndGet[List[SicCode]](SIC_CODES_KEY, Some(List(validLabourSicCode)))
      when(mockSicAndComplianceSrv.saveMainBusinessActivity(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
      when(mockSicAndComplianceSrv.needComplianceQuestions(any())).thenReturn(true)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validLabourSicCode.id)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")

    }
    "return 303 with selected sicCode (noCompliance) and sicCode list in keystore" in new Setup {
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validNoCompliance)))
      when(mockSicAndComplianceSrv.saveMainBusinessActivity(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithoutLabour))
      when(mockSicAndComplianceSrv.needComplianceQuestions(any())).thenReturn(false)

      submitAuthorised(Controller.submit(),

        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validNoCompliance.id)
      )(_ redirectsTo s"$contextRoot/business-bank-account")
    }

    "return 303 with selected sicCode (Labour) and sicCode list in keystore" in new Setup {
      mockKeystoreFetchAndGet(SIC_CODES_KEY, Some(List(validLabourSicCode)))
      when(mockSicAndComplianceSrv.saveMainBusinessActivity(any())(any(), any())).thenReturn(Future.successful(s4lVatSicAndComplianceWithLabour))
      when(mockSicAndComplianceSrv.needComplianceQuestions(any())).thenReturn(true)

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("mainBusinessActivityRadio" -> validLabourSicCode.id)
      )(_ redirectsTo s"$contextRoot/tell-us-more-about-the-company")
    }
  }
}
