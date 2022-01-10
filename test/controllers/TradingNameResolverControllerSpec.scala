/*
 * Copyright 2022 HM Revenue & Customs
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
import models.api._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.iteratee.Execution.Implicits.defaultExecutionContext
import play.api.test.FakeRequest
import services.mocks.{MockApplicantDetailsService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class TradingNameResolverControllerSpec extends ControllerSpec
  with MockVatRegistrationService
  with MockApplicantDetailsService
  with VatRegistrationFixture {

  class Setup {
    val testUrl = "/resolve-party-type"
    val testBusinessName = "testBusinessName"

    object Controller extends TradingNameResolverController(
      mockSessionService,
      mockAuthClientConnector,
      vatRegistrationServiceMock,
      mockApplicantDetailsService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "resolve" must {
    List(Individual, NETP).foreach { partyType =>
      s"redirects to ${controllers.registration.business.routes.MandatoryTradingNameController.show.url} for partyType ${partyType.toString}" in new Setup {
        mockPartyType(Future.successful(partyType))
        val res = Controller.resolve(FakeRequest())
        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.business.routes.MandatoryTradingNameController.show.url)
      }
    }

    List(Partnership, ScotPartnership).foreach { partyType =>
      s"redirects to ${controllers.registration.business.routes.PartnershipNameController.show.url} for partyType ${partyType.toString}" in new Setup {
        mockPartyType(Future.successful(Partnership))
        val res = Controller.resolve()(FakeRequest())
        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.business.routes.PartnershipNameController.show.url)
      }
    }

    List(ScotLtdPartnership, LtdPartnership).foreach { partyType =>
      s"redirects to ${controllers.registration.business.routes.PartnershipNameController.show.url} for partyType ${partyType.toString}" in new Setup {
        mockPartyType(Future.successful(Partnership))
        val res = Controller.resolve()(FakeRequest())
        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.business.routes.PartnershipNameController.show.url)
      }
    }

    List(UkCompany, RegSociety, CharitableOrg, ScotLtdPartnership, LtdPartnership, LtdLiabilityPartnership).foreach { partyType =>
      s"redirects to ${controllers.registration.business.routes.TradingNameController.show.url} for partyType ${partyType.toString} when business name is present" in new Setup {
        when(mockApplicantDetailsService.getCompanyName(any(), any()))
          .thenReturn(Future.successful(Some(testBusinessName)))

        mockPartyType(Future.successful(partyType))
        val res = Controller.resolve(FakeRequest())
        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.business.routes.TradingNameController.show.url)
      }
    }

    List(Trust, UnincorpAssoc, NonUkNonEstablished).foreach { partyType =>
      s"redirects to ${controllers.registration.business.routes.BusinessNameController.show.url} for partyType ${partyType.toString} when business name is missing" in new Setup {
        when(mockApplicantDetailsService.getCompanyName(any(), any()))
          .thenReturn(Future.successful(None))

        mockPartyType(Future.successful(partyType))
        val res = Controller.resolve(FakeRequest())
        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.registration.business.routes.BusinessNameController.show.url)
      }
    }

    "throw an exception for unsupported partyType" in new Setup {
      mockPartyType(Future.successful(AdminDivision))
      intercept[InternalServerException] {
        await(Controller.resolve(FakeRequest()))
      }
    }
  }
}
