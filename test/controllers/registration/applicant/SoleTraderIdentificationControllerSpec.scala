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

package controllers.registration.applicant

import fixtures.VatRegistrationFixture
import models.api.{Individual, UkCompany}
import play.api.test.FakeRequest
import services.mocks.{MockApplicantDetailsService, MockSoleTraderIdService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderIdentificationControllerSpec extends ControllerSpec
  with MockApplicantDetailsService
  with MockSoleTraderIdService
  with MockVatRegistrationService
  with VatRegistrationFixture {

  class Setup {
    val testJourneyId = "testJourneyId"
    val testJourneyUrl = "/test-journey-url"

    object Controller extends SoleTraderIdentificationController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockApplicantDetailsService,
      mockSoleTraderIdService,
      vatRegistrationServiceMock
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "startJourney" must {
    "redirect to the STI journey url" in new Setup {
      mockGetVatScheme(Future.successful(validVatScheme))
      mockStartJourney(appConfig.getSoleTraderIdentificationCallbackUrl, "Register for VAT", "vrs", appConfig.feedbackUrl)(Future.successful(testJourneyUrl))
      mockPartyType(Future.successful(Individual))

      val res = Controller.startJourney()(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(testJourneyUrl)
    }
    "throw an exception if the call to STI fails" in new Setup {
      mockGetVatScheme(Future.successful(validVatScheme))
      mockStartJourney(appConfig.getSoleTraderIdentificationCallbackUrl, "Register for VAT", "vrs", appConfig.feedbackUrl)(Future.failed(new InternalServerException("")))
      mockPartyType(Future.successful(UkCompany))

      intercept[InternalServerException] {
        await(Controller.startJourney()(FakeRequest()))
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "redirect to the former name page if the user is a Sole Trader" in new Setup {
      mockGetVatScheme(Future.successful(validSoleTraderVatScheme))
      mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful((testTransactorDetails, None)))
      mockSaveApplicantDetails(testTransactorDetails)(emptyApplicantDetails.copy(transactor = Some(testTransactorDetails)))
      mockPartyType(Future.successful(Individual))

      val res = Controller.callback(testJourneyId)(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.registration.applicant.routes.FormerNameController.show().url)
    }

    "redirect to the capture role page" in new Setup {
      mockGetVatScheme(Future.successful(validVatScheme))
      mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful((testTransactorDetails, None)))
      mockSaveApplicantDetails(testTransactorDetails)(emptyApplicantDetails.copy(transactor = Some(testTransactorDetails)))
      mockPartyType(Future.successful(UkCompany))

      val res = Controller.callback(testJourneyId)(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.registration.applicant.routes.CaptureRoleInTheBusinessController.show().url)
    }
  }

}
