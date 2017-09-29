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

package controllers.vatLodgingOfficer

import java.time.LocalDate

import connectors.KeystoreConnector
import fixtures.VatRegistrationFixture
import helpers.{S4LMockSugar, VatRegSpec}
import models.CurrentProfile
import models.ModelKeys.REGISTERING_OFFICER_KEY
import models.api.{DateOfBirth, Name, VatScheme}
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerSecurityQuestionsView
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

import scala.concurrent.Future

class OfficerSecurityQuestionsControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends OfficerSecurityQuestionsController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerSecurityQuestionsController.show())

  s"GET ${routes.OfficerSecurityQuestionsController.show()}" should {
    "succeed for all possible Officer / OfficerSecurityQuestionsView combinations" in {

      val nameSame = Name(Some("forename"), None, "surname")
      val nameOther = Name(Some("other"), None, "other")

      val officerWithDOB = Officer(name = nameSame, role = "", dateOfBirth = Some(DateOfBirth(1, 1, 1900)))
      val officerWithoutDOB = Officer(name = nameSame, role = "", dateOfBirth = None)

      val securityQuestionsViewSameName = OfficerSecurityQuestionsView(LocalDate.of(2000, 1, 1), testNino, Some(nameSame))
      val securityQuestionsViewOtherName = OfficerSecurityQuestionsView(LocalDate.of(2000, 1, 1), testNino, Some(nameOther))

      val securityQuestionsViewFromOfficerWithDOB = OfficerSecurityQuestionsView(LocalDate.of(1900, 1, 1), testNino, Some(officerWithDOB.name))
      val securityQuestionsViewOfficerWithDOBAndNINOIsNone = OfficerSecurityQuestionsView(LocalDate.of(1900, 1, 1), "", Some(officerWithDOB.name))

      case class TestCase(officer: Option[Officer], securityQuestionsView: Option[OfficerSecurityQuestionsView], expected: Option[OfficerSecurityQuestionsView])

      val testCases = List(
        TestCase(None, None, None),
        TestCase(None, Some(securityQuestionsViewSameName), Some(securityQuestionsViewSameName)),

        TestCase(Some(officerWithDOB), Some(securityQuestionsViewFromOfficerWithDOB), Some(securityQuestionsViewFromOfficerWithDOB)),
        TestCase(Some(officerWithDOB), Some(securityQuestionsViewSameName), Some(securityQuestionsViewSameName)),
        TestCase(Some(officerWithDOB), Some(securityQuestionsViewOtherName), Some(securityQuestionsViewFromOfficerWithDOB)),

        TestCase(Some(officerWithoutDOB), None, None),
        TestCase(Some(officerWithoutDOB), Some(securityQuestionsViewSameName), Some(securityQuestionsViewSameName)),
        TestCase(Some(officerWithoutDOB), Some(securityQuestionsViewOtherName), None),
        TestCase(Some(officerWithDOB), None, Some(securityQuestionsViewOfficerWithDOBAndNINOIsNone))

      )

      def test(testCase: TestCase): Boolean = {
        val officerOpt = testCase.officer
        val securityQuestionsViewOpt = testCase.securityQuestionsView

        // setup mocks
        securityQuestionsViewOpt.fold(save4laterReturnsNoViewModel[OfficerSecurityQuestionsView]())(view => save4laterReturnsViewModel(view)())
        officerOpt.fold(mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer]))(
          (officer: Officer) => mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Some(officer)))

        // test controller logic here
        Controller.getView(officerOpt, securityQuestionsViewOpt) == testCase.expected
      }

      // test all scenarios
      forAll (testCases) (tc => test(tc) mustBe true)
    }


    "return HTML and form populated" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterReturnsViewModel(OfficerSecurityQuestionsView(testDate, testNino, Some(officerName)))()
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

    "return HTML with empty form" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterReturnsNoViewModel[OfficerSecurityQuestionsView]()
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

  }

  s"POST ${routes.OfficerSecurityQuestionsController.submit()}" should {
    "return 400 with Empty data" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }

    val officer = Officer(Name(None, None, "surname"), "director", Some(DateOfBirth(12, 11, 1973)))

    "return 303 with officer in keystore" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterExpectsSave[OfficerSecurityQuestionsView]()
      mockKeystoreFetchAndGet[Officer](REGISTERING_OFFICER_KEY, Some(officer))

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980", "nino" -> testNino)
      )(_ redirectsTo s"$contextRoot/changed-name")
    }

    "return 303 with no officer in keystore" in {
      when(mockKeystoreConnector.fetchAndGet[CurrentProfile](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(currentProfile)))

      save4laterExpectsSave[OfficerSecurityQuestionsView]()
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])

      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980", "nino" -> testNino)
      )(_ redirectsTo s"$contextRoot/changed-name")
    }
  }
}