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

package controllers

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.VatRegistrationService
import scala.concurrent.Future
import cats.data.OptionT

class ApplicationSubmissionControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object Controller extends ApplicationSubmissionController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
  }

  val fakeRequest = FakeRequest(routes.ApplicationSubmissionController.show())

  s"GET ${routes.ApplicationSubmissionController.show()}" should {

    "display the submission confirmation page to the user" in {

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.pure)
      when(mockVatRegistrationService.getAckRef(Matchers.eq(validVatScheme.id))(any())).thenReturn(OptionT.some("testAckRef"))

      callAuthorised(Controller.show) {
        _ includesText "Application submitted"
      }
    }

    "should fail to complete if no ackRef number can be retrieved for current registration" in {

      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      when(mockVatRegistrationService.getAckRef(Matchers.eq(emptyVatScheme.id))(any())).thenReturn(OptionT.none[Future,String])

      callAuthorised(Controller.show) {
        _ isA 500
      }
    }
  }

}
