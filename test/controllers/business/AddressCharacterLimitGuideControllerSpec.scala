/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.business

import fixtures.VatRegistrationFixture
import models.api.{CharitableOrg, EligibilitySubmissionData, Individual, LtdLiabilityPartnership, LtdPartnership, NETP, NonUkNonEstablished, Partnership, PartyType, RegSociety, ScotLtdPartnership, Trust, UkCompany, UnincorpAssoc}
import models.CurrentProfile
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import services.mocks.MockVatRegistrationService
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.business.AddressCharacterLimitGuideView
import scala.concurrent.Future
import org.scalatest.prop.TableDrivenPropertyChecks._

class AddressCharacterLimitGuideControllerSpec extends ControllerSpec
  with VatRegistrationFixture
  with FutureAssertions
  with MockVatRegistrationService {

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(controllers.business.routes.AddressCharacterLimitGuideController.show)

  class Setup {
    val view: AddressCharacterLimitGuideView = app.injector.instanceOf[AddressCharacterLimitGuideView]
    val testController = new AddressCharacterLimitGuideController(
      mockAuthClientConnector,
      mockSessionService,
      mockVatRegistrationService,
      view
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  def mockGetEligibilitySubmissionData(response: Future[EligibilitySubmissionData]): Unit =
    when(mockVatRegistrationService.getEligibilitySubmissionData(any[CurrentProfile], any[HeaderCarrier], any[Request[_]]))
      .thenReturn(response)

  "show" must {
    "return OK and the correct view" in new Setup {
      callAuthorised(testController.show) { result =>
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
      }
    }
  }

  "submit" must {
    val testCases = Table(
      ("Description", "PartyType", "FixedEstablishment", "ExpectedRedirect"),
      ("NETP without fixed establishment", Individual, false, controllers.business.routes.InternationalPpobAddressController.show.url),
      ("NonUkNonEstablished without fixed establishment", NonUkNonEstablished, false, controllers.business.routes.InternationalPpobAddressController.show.url),
      ("NETP with fixed establishment", Individual, true, controllers.business.routes.PpobAddressController.startJourney.url),
      ("NonUkNonEstablished with fixed establishment", NonUkNonEstablished, true, controllers.business.routes.PpobAddressController.startJourney.url),
      ("UK Company", UkCompany, true, controllers.business.routes.PpobAddressController.startJourney.url),
      ("Solo trader", Individual, true, controllers.business.routes.PpobAddressController.startJourney.url),
      ("Partnership", Partnership, true, controllers.business.routes.PpobAddressController.startJourney.url)
    )

    forAll(testCases) { (description, partyType, fixedEstablishment, expectedRedirect) =>
      s"redirect to correct page for $description" in new Setup {
        mockGetEligibilitySubmissionData(Future.successful(
          validEligibilitySubmissionData.copy(partyType = partyType, fixedEstablishmentInManOrUk = fixedEstablishment)
        ))

        callAuthorised(testController.submit) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(expectedRedirect)
        }
      }
    }

    "redirect to PPOB Address page for other party types" in new Setup {
      val otherPartyTypes: Seq[PartyType] = Seq(
        RegSociety, CharitableOrg, ScotLtdPartnership, LtdPartnership, LtdLiabilityPartnership, Trust, UnincorpAssoc
      )

      otherPartyTypes.foreach { partyType =>
        mockGetEligibilitySubmissionData(Future.successful(validEligibilitySubmissionData.copy(partyType = partyType)))

        callAuthorised(testController.submit) { result =>
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.business.routes.PpobAddressController.startJourney.url)
        }
      }
    }
  }
}
