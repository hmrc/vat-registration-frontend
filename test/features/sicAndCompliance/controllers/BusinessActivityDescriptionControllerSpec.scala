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
import models.view.sicAndCompliance.BusinessActivityDescription
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class BusinessActivityDescriptionControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val DESCRIPTION = "Testing"

  trait Setup {
    object TestController extends BusinessActivityDescriptionController(
      ds,
      mockKeystoreConnector,
      mockSicAndComplianceSrv,
      mockAuthConnector,
      mockS4LService,
      mockVatRegistrationService
    )

    mockGetCurrentProfile()
  }

  val fakeRequest = FakeRequest(routes.BusinessActivityDescriptionController.show())

  s"GET ${routes.BusinessActivityDescriptionController.show()}" should {
    "return HTML Business Activity Description page with no data in the form" in new Setup {
      mockGetSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(TestController.show(), fakeRequest.withFormUrlEncodedBody(
        "description" -> ""
      ))(_ includesText "Enter a description of the type of goods or services the company sells.")
    }
  }

  s"POST ${routes.BusinessActivityDescriptionController.submit()} with Empty data" should {
    "return 400" in new Setup {
      submitAuthorised(TestController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303" in new Setup {
      mockUpdateSicAndCompliance(Future.successful(s4lVatSicAndComplianceWithLabour))

      submitAuthorised(TestController.submit(), fakeRequest.withFormUrlEncodedBody("description" -> DESCRIPTION)) {
        _ redirectsTo "/sic-stub"
      }
    }
  }
}
