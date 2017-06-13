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
import models.ModelKeys.REGISTERING_OFFICER_KEY
import models.api.{DateOfBirth, Name, VatScheme}
import models.external.Officer
import models.view.vatLodgingOfficer.OfficerDateOfBirthView
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.test.FakeRequest

class OfficerDateOfBirthControllerSpec extends VatRegSpec with VatRegistrationFixture with S4LMockSugar {

  object Controller extends OfficerDateOfBirthController(ds)(mockS4LService, mockVatRegistrationService) {
    override val authConnector = mockAuthConnector
    override val keystoreConnector: KeystoreConnector = mockKeystoreConnector
  }

  val fakeRequest = FakeRequest(controllers.vatLodgingOfficer.routes.OfficerDateOfBirthController.show())

  s"GET ${routes.OfficerDateOfBirthController.show()}" should {

    "succeed for all possible Officer / OfficerDateOfBirthView combinations" in {

      val nameSame = Name(Some("forename"), None, "surname")
      val nameOther = Name(Some("other"), None, "other")

      val officerWithDOB = Officer(name = nameSame, role = "", dateOfBirth = Some(DateOfBirth(1, 1, 1900)))
      val officerWithoutDOB = Officer(name = nameSame, role = "", dateOfBirth = None)

      val dobViewSameName = OfficerDateOfBirthView(LocalDate.of(2000, 1, 1), Some(nameSame))
      val dobViewOtherName = OfficerDateOfBirthView(LocalDate.of(2000, 1, 1), Some(nameOther))

      val dobViewFromOfficerWithDOB = OfficerDateOfBirthView(LocalDate.of(1900, 1, 1), Some(officerWithDOB.name))

      case class TestCase(officer: Option[Officer], dobView: Option[OfficerDateOfBirthView], expected: Option[OfficerDateOfBirthView])
      val testCases = List(
        TestCase(None, None, None),
        TestCase(None, Some(dobViewSameName), Some(dobViewSameName)),

        TestCase(Some(officerWithDOB), None, Some(dobViewFromOfficerWithDOB)),
        TestCase(Some(officerWithDOB), Some(dobViewSameName), Some(dobViewSameName)),
        TestCase(Some(officerWithDOB), Some(dobViewOtherName), Some(dobViewFromOfficerWithDOB)),

        TestCase(Some(officerWithoutDOB), None, None),
        TestCase(Some(officerWithoutDOB), Some(dobViewSameName), Some(dobViewSameName)),
        TestCase(Some(officerWithoutDOB), Some(dobViewOtherName), None)
      )

      def test(testCase: TestCase): Boolean = {
        val officerOpt = testCase.officer
        val dobViewOpt = testCase.dobView

        // setup mocks
        dobViewOpt.fold(save4laterReturnsNoViewModel[OfficerDateOfBirthView]())(view => save4laterReturnsViewModel(view)())
        officerOpt.fold(mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer]))(
          (officer: Officer) => mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Some(officer)))

        when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(validVatScheme.copy(lodgingOfficer = None).pure)

        // test controller logic here
        Controller.getView(officerOpt, dobViewOpt) == testCase.expected
      }

      // test all scenarios
      forAll (testCases) (tc => test(tc) mustBe true)
    }


    "return HTML and form populated" in {
      val vatScheme = validVatScheme.copy(lodgingOfficer = Some(validLodgingOfficer))
      save4laterReturnsNoViewModel[OfficerDateOfBirthView]()
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(vatScheme.pure)

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

    "return HTML with empty form" in {
      val emptyVatScheme = VatScheme("0")
      save4laterReturnsNoViewModel[OfficerDateOfBirthView]()
      when(mockVatRegistrationService.getVatScheme()(any())).thenReturn(emptyVatScheme.pure)
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])

      callAuthorised(Controller.show()) {
        _ includesText "What is your date of birth"
      }
    }

  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with Empty data" should {
    "return 400" in {
      submitAuthorised(Controller.submit(), fakeRequest.withFormUrlEncodedBody())(result => result isA 400)
    }
  }

  s"POST ${routes.OfficerDateOfBirthController.submit()} with valid DateOfBirth entered" should {

    val officer = Officer(Name(None, None, "surname"), "director", Some(DateOfBirth(12, 11, 1973)))

    "return 303 with officer in keystore" in {
      save4laterExpectsSave[OfficerDateOfBirthView]()
      mockKeystoreFetchAndGet[Officer](REGISTERING_OFFICER_KEY, Some(officer))
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980")
      )(_ redirectsTo s"$contextRoot/your-national-insurance-number")
    }

    "return 303 with no officer in keystore" in {
      save4laterExpectsSave[OfficerDateOfBirthView]()
      mockKeystoreFetchAndGet(REGISTERING_OFFICER_KEY, Option.empty[Officer])
      submitAuthorised(Controller.submit(),
        fakeRequest.withFormUrlEncodedBody("dob.day" -> "1", "dob.month" -> "1", "dob.year" -> "1980")
      )(_ redirectsTo s"$contextRoot/your-national-insurance-number")
    }
  }

}
