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

package controllers.grs

import fixtures.VatRegistrationFixture
import models.api._
import models.external.soletraderid.{JourneyLabels, SoleTraderIdJourneyConfig, TranslationLabels}
import play.api.mvc.Result
import play.api.test.FakeRequest
import services.mocks.{MockApplicantDetailsService, MockEntityService, MockSoleTraderIdService, MockVatRegistrationService}
import testHelpers.ControllerSpec
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SoleTraderIdControllerSpec extends ControllerSpec
  with MockApplicantDetailsService
  with MockSoleTraderIdService
  with MockVatRegistrationService
  with MockEntityService
  with VatRegistrationFixture {

  class Setup {
    val testJourneyId = "testJourneyId"
    val testJourneyUrl = "/test-journey-url"

    val soleTraderIdJourneyConfig: SoleTraderIdJourneyConfig = SoleTraderIdJourneyConfig(
      continueUrl = appConfig.soleTraderCallbackUrl,
      deskProServiceId = "vrs",
      signOutUrl = appConfig.feedbackUrl,
      accessibilityUrl = appConfig.accessibilityStatementUrl,
      regime = appConfig.regime,
      businessVerificationCheck = true,
      labels = Some(JourneyLabels(
        en = TranslationLabels(
          optFullNamePageLabel = Some("Who are you registering on behalf of?"),
          optServiceName = Some("Register for VAT")
        ),
        cy = TranslationLabels(
          optFullNamePageLabel = Some("Ar ran pwy rydych yn cofrestru?"),
          optServiceName = Some("Cofrestru ar gyfer TAW")
        )
      ))
    )

    object Controller extends SoleTraderIdController(
      mockSessionService,
      mockAuthClientConnector,
      mockApplicantDetailsService,
      mockSoleTraderIdService,
      vatRegistrationServiceMock
    )

    mockAuthenticated()
    mockWithCurrentProfile(Some(currentProfile))
  }

  "startJourney" must {
    "redirect to the STI journey url for Sole Traders" in new Setup {
      mockPartyType(Future.successful(Individual))
      mockIsTransactor(Future.successful(true))
      mockStartSoleTraderJourney(
        soleTraderIdJourneyConfig,
        partyType = Individual
      )(Future.successful(testJourneyUrl))

      val res: Future[Result] = Controller.startJourney()(FakeRequest())

      status(res) mustBe SEE_OTHER
      redirectLocation(res) mustBe Some(testJourneyUrl)
    }

    "throw an exception if the call to STI fails" in new Setup {
      mockPartyType(Future.successful(UkCompany))
      mockIsTransactor(Future.successful(true))
      mockStartSoleTraderJourney(
        soleTraderIdJourneyConfig,
        partyType = UkCompany
      )(Future.failed(new InternalServerException("")))

      intercept[InternalServerException] {
        await(Controller.startJourney()(FakeRequest()))
      }
    }
  }

  "retrieveSoleTraderDetails" must {
    List(Individual, NETP).foreach { partyType =>
      s"redirect to the former name page if the user is a $partyType" in new Setup {
        mockGetVatScheme(Future.successful(validSoleTraderVatScheme))
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful((testPersonalDetails, testSoleTrader)))
        mockSaveApplicantDetails(testPersonalDetails)(emptyApplicantDetails.copy(personalDetails = Some(testPersonalDetails)))
        mockSaveApplicantDetails(testSoleTrader)(emptyApplicantDetails.copy(entity = Some(testSoleTrader)))
        mockPartyType(Future.successful(partyType))

        val res: Future[Result] = Controller.callback(testJourneyId)(FakeRequest())

        status(res) mustBe SEE_OTHER
        redirectLocation(res) must contain(controllers.routes.TaskListController.show.url)
      }
    }
  }

}
