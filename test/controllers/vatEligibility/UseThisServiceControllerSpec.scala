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

package controllers.vatEligibility

import helpers.VatRegSpec
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UseThisServiceControllerSpec extends VatRegSpec {

  object TestController extends UseThisServiceController(mockVatRegistrationService, ds) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.UseThisServiceController.show())

  "GET /use-this-service" should {

    "return HTML when user accesses the page" in {
      when(mockVatRegistrationService.createRegistrationFootprint()(any()))
        .thenReturn(Future.successful(()))

      callAuthorised(TestController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
      }
    }

  }
}
