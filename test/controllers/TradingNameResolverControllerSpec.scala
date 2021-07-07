/*
 * Copyright 2021 HM Revenue & Customs
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
import models.api.{AdminDivision, Individual, UkCompany}
import play.api.libs.iteratee.Execution.Implicits.defaultExecutionContext
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class TradingNameResolverControllerSpec extends ControllerSpec
  with MockVatRegistrationService
  with VatRegistrationFixture {

  class Setup {
    val testUrl = "/resolve-party-type"
    object Controller extends TradingNameResolverController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      vatRegistrationServiceMock
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "show" must {
    "redirect to /changed-name url for partyType Individual" in new Setup {
      mockPartyType(Future.successful(Individual))
      val res = Controller.resolve()(FakeRequest())
      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.registration.applicant.routes.SoleTraderNameController.show().url)
    }

    "redirect to /role-in-the-business url for partyType UkCompany" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      val res = Controller.resolve()(FakeRequest())
      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.registration.business.routes.TradingNameController.show().url)
    }

    "throw an exception for unsupported partyType" in new Setup {
      mockPartyType(Future.successful(AdminDivision))
      intercept[InternalServerException] {
        await(Controller.resolve()(FakeRequest()))
      }
    }
  }
}
