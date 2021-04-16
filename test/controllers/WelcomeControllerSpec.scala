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

import common.enums.VatRegStatus
import featureswitch.core.config.{FeatureSwitching, SaveAndContinueLater}
import fixtures.VatRegistrationFixture
import models.CurrentProfile
import models.api.VatScheme
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import services.{Failed, PassedOTRS, PassedVatReg}
import testHelpers.{ControllerSpec, FutureAssertions}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class WelcomeControllerSpec extends ControllerSpec with FutureAssertions with VatRegistrationFixture with FeatureSwitching {

  val testController: WelcomeController = new WelcomeController(
    mockVatRegistrationService,
    mockCurrentProfileService,
    mockAuthClientConnector,
    mockKeystoreConnector,
    mockTrafficManagementService,
    mockSaveAndRetrieveService
  )

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(routes.WelcomeController.show())
  val testCurrentProfile: CurrentProfile = CurrentProfile("testRegid", VatRegStatus.draft)

  "GET /" should {
    "return HTML" when {
      "user is authorized to access and has no profile" in {
        mockAuthenticated()
        mockWithCurrentProfile(None)

        when(mockVatRegistrationService.createRegistrationFootprint(any()))
          .thenReturn(Future.successful(VatScheme(id = testRegId, status = VatRegStatus.draft)))
        when(mockCurrentProfileService.buildCurrentProfile(any())(any()))
          .thenReturn(Future.successful(testCurrentProfile))

        when(mockVatRegistrationService.getTaxableThreshold(any())(any())) thenReturn Future.successful(formattedThreshold)

        callAuthorisedOrg(testController.show) {
          result =>
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(appConfig.eligibilityUrl)
        }
      }

      "user is authorized to access and has a profile" in {
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))

        when(mockVatRegistrationService.getTaxableThreshold(any())(any())) thenReturn Future.successful(formattedThreshold)

        callAuthorisedOrg(testController.show) {
          result =>
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(appConfig.eligibilityUrl)
        }
      }

      "user is authorized to access and has a profile and has Passed Eligibility" in {
        val vatSchemeJson = Json.toJson(validVatScheme)
        enable(SaveAndContinueLater)
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))

        when(mockTrafficManagementService.checkTrafficManagement(ArgumentMatchers.any())).thenReturn(Future.successful(PassedVatReg(testRegId)))

        when(mockCurrentProfileService.buildCurrentProfile(any())(any())).thenReturn(Future.successful(testCurrentProfile))

        when(mockSaveAndRetrieveService.retrievePartialVatScheme(ArgumentMatchers.any[String])(any[HeaderCarrier])).thenReturn(Future.successful(vatSchemeJson))

        callAuthorisedOrg(testController.show) {
          result =>
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(appConfig.eligibilityRouteUrl)
        }
      }

      "user is authorized to access and has a profile and has passed into OTRS" in {
        enable(SaveAndContinueLater)
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))

        when(mockCurrentProfileService.buildCurrentProfile(any())(any()))
          .thenReturn(Future.successful(testCurrentProfile))


        when(mockTrafficManagementService.checkTrafficManagement(ArgumentMatchers.any())) thenReturn Future.successful(PassedOTRS)


        callAuthorisedOrg(testController.show) {
          result =>
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(appConfig.otrsRoute)
        }
      }

      "user is authorized to access and has a profile and has returned Failed" in {
        enable(SaveAndContinueLater)
        mockAuthenticated()
        mockWithCurrentProfile(Some(currentProfile))

        when(mockCurrentProfileService.buildCurrentProfile(any())(any()))
          .thenReturn(Future.successful(testCurrentProfile))


        when(mockTrafficManagementService.checkTrafficManagement(ArgumentMatchers.any())) thenReturn Future.successful(Failed)


        callAuthorisedOrg(testController.show) {
          result =>
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(appConfig.eligibilityUrl)
        }
      }
    }
  }
}
