/*
 * Copyright 2023 HM Revenue & Customs
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

import _root_.models._
import featureswitch.core.config.FeatureSwitching
import fixtures.VatRegistrationFixture
import models.api.{CharitableOrg, GovOrg, RegSociety, UkCompany}
import models.external.incorporatedentityid.{IncorpIdJourneyConfig, JourneyLabels, TranslationLabels}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import services.mocks.{MockApplicantDetailsService, MockEntityService, MockVatRegistrationService, TimeServiceMock}
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.Future

class IncorpIdControllerSpec extends ControllerSpec
  with VatRegistrationFixture
  with TimeServiceMock
  with FutureAssertions
  with MockApplicantDetailsService
  with MockVatRegistrationService
  with FeatureSwitching
  with MockEntityService {

  val testJourneyId = "testJourneyId"

  class Setup(cp: Option[CurrentProfile] = Some(currentProfile)) {
    val testController: IncorpIdController = new IncorpIdController(
      mockAuthClientConnector,
      mockSessionService,
      mockIncorpIdService,
      mockApplicantDetailsService,
      vatRegistrationServiceMock
    )

    mockAuthenticated()
    mockWithCurrentProfile(cp)
  }

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val testJourneyConfig: IncorpIdJourneyConfig = IncorpIdJourneyConfig(
    continueUrl = appConfig.incorpIdCallbackUrl,
    deskProServiceId = "vrs",
    signOutUrl = appConfig.feedbackUrl,
    accessibilityUrl = appConfig.accessibilityStatementUrl,
    regime = appConfig.regime,
    businessVerificationCheck = true,
    labels = Some(JourneyLabels(
      en = TranslationLabels(
        optServiceName = Some("Register for VAT")
      ),
      cy = TranslationLabels(
        optServiceName = Some("Cofrestru ar gyfer TAW")
      )
    ))
  )

  "startJourney" should {
    List(UkCompany, RegSociety, CharitableOrg).foreach { partyType =>
      s"redirect to the journeyStartUrl for $partyType" in new Setup {
        lazy val testJourneyStartUrl = "/test"
        mockCreateJourney(testJourneyConfig, partyType)(Future.successful(testJourneyStartUrl))
        mockPartyType(Future.successful(partyType))

        lazy val res: Future[Result] = testController.startJourney(fakeRequest)

        status(res) mustBe SEE_OTHER
        redirectLocation(res) mustBe Some(testJourneyStartUrl)
      }
    }

    "return INTERNAL_SERVER_ERROR for invalid partyType" in new Setup {
      lazy val testJourneyStartUrl = "/test"
      mockCreateJourney(testJourneyConfig, GovOrg)(Future.successful(testJourneyStartUrl))
      mockPartyType(Future.successful(GovOrg))

      val ex = intercept[InternalServerException] {
        await(testController.startJourney(fakeRequest))
      }
      ex.getMessage mustBe "[IncorpIdController][startJourney] attempted to start journey with invalid partyType: GovOrg"
    }
  }

  "incorpIdCallback" when {
    "store the incorporation details and redirect to IndividualIdentification when the response is valid" in new Setup {
      mockGetDetails(testJourneyId)(Future.successful(testLimitedCompany))
      mockSaveApplicantDetails(testLimitedCompany)(completeApplicantDetails)
      mockIsTransactor(Future(true))

      val res = testController.incorpIdCallback(testJourneyId)(fakeRequest)

      status(res) mustBe SEE_OTHER
      redirectLocation(res) must contain(controllers.routes.TaskListController.show.url)
    }
  }

}
