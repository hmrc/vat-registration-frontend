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

import config.FrontendAppConfig
import fixtures.VatRegistrationFixture
import models.TransactorDetails
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.mocks.{MockApplicantDetailsService, MockSoleTraderIdService}
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderIdentificationControllerSpec extends ControllerSpec
  with MockApplicantDetailsService
  with MockSoleTraderIdService
  with VatRegistrationFixture {

  class Setup {
    val testJourneyId = "testJourneyId"
    val testJourneyUrl = "/test-journey-url"

    object Controller extends SoleTraderIdentificationController(
      mockKeystoreConnector,
      mockAuthClientConnector,
      mockApplicantDetailsService,
      mockSoleTraderIdService
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "startJourney" must {
    "redirect to the STI journey url" in new Setup {
      mockStartJourney(appConfig.getSoleTraderIdentificationCallbackUrl, "Register for VAT", "vrs", appConfig.feedbackUrl)(Future.successful(testJourneyUrl))

      val res = Controller.startJourney()(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(testJourneyUrl)
    }
    "throw an exception if the call to STI fails" in new Setup {
      mockStartJourney(appConfig.getSoleTraderIdentificationCallbackUrl, "Register for VAT", "vrs", appConfig.feedbackUrl)(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Controller.startJourney()(FakeRequest()))
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    "redirect to the capture role page" in new Setup {
      mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(testTransactorDetails))
      mockSaveApplicantDetails(testTransactorDetails)(emptyApplicantDetails.copy(transactorDetails = Some(testTransactorDetails)))

      val res = Controller.callback(testJourneyId)(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.registration.applicant.routes.CaptureRoleInTheBusinessController.show().url)
    }
  }

}
