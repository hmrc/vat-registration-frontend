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

import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.S4LKey
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.labour.Workers
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class BusinessActivityDescriptionControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  val DESCRIPTION = "Testing"

  object TestController extends BusinessActivityDescriptionController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  override def beforeEach() {
    reset(mockVatRegistrationService)
    reset(mockS4LService)
  }

  val fakeRequest = FakeRequest(routes.BusinessActivityDescriptionController.show())

  s"GET ${routes.BusinessActivityDescriptionController.show()}" should {

    "return HTML Business Activity Description page with no data in the form" in {
      save4laterReturnsViewModel(BusinessActivityDescription(DESCRIPTION))()
      submitAuthorised(TestController.show(), fakeRequest.withFormUrlEncodedBody(
        "description" -> ""
      ))(_ includesText "Describe what the company does")
    }


    "return HTML when there's nothing in S4L and vatScheme contains data" in {
      save4laterReturnsNothing2[BusinessActivityDescription]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(validVatScheme))

      callAuthorised(TestController.show) {
        _ includesText "Describe what the company does"
      }
    }

    "return HTML when there's nothing in S4L and vatScheme contains no data" in {
      save4laterReturnsNothing2[BusinessActivityDescription]()
      when(mockVatRegistrationService.getVatScheme()(any[HeaderCarrier]())).thenReturn(Future.successful(emptyVatScheme))

      callAuthorised(TestController.show) {
        _ includesText "Describe what the company does"
      }
    }
  }

  s"POST ${routes.BusinessActivityDescriptionController.submit()} with Empty data" should {

    "return 400" in {
      submitAuthorised(TestController.submit(), fakeRequest.withFormUrlEncodedBody(
      ))(result => result isA 400)
    }

    "return 303" in {
      save4laterExpectsSave[BusinessActivityDescription]()
      submitAuthorised(TestController.submit(), fakeRequest.withFormUrlEncodedBody("description" -> DESCRIPTION)) {
        _ redirectsTo "/sic-stub"
      }
    }
  }
}
