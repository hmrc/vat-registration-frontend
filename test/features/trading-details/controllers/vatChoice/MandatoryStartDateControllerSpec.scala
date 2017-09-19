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

package controllers.vatTradingDetails.vatChoice

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.CurrentProfile
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class MandatoryStartDateControllerSpec extends VatRegSpec with VatRegistrationFixture {

  object MandatoryStartDateController extends MandatoryStartDateController(mockS4LService, ds) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(routes.StartDateController.show())

  s"GET ${routes.MandatoryStartDateController.show()}" should {
    "display the mandatory start date confirmation page to the user" in {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      callAuthorised(MandatoryStartDateController.show) {
        result =>
          status(result) mustBe OK
          contentType(result) mustBe Some("text/html")
          charset(result) mustBe Some("utf-8")
          contentAsString(result) must include("VAT start date")
      }
    }
  }

  s"POST ${routes.MandatoryStartDateController.submit()}" should {
    "redirect the user to the bank account page after clicking continue on the mandatory start date confirmation page" in {

      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      when(mockVatRegistrationService.submitTradingDetails()(any(), any())).thenReturn(validVatTradingDetails.pure)
      callAuthorised(MandatoryStartDateController.submit) {
        result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result).getOrElse("") mustBe s"$contextRoot/business-bank-account"
      }

      verify(mockVatRegistrationService).submitTradingDetails()(any(), any())
    }
  }

}
