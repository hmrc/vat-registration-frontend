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

package services

import common.enums.VatRegStatus
import connectors.mocks.MockRegistrationApiConnector
import fixtures.ApplicantDetailsFixtures
import models._
import models.api.{Address, Individual, UkCompany}
import models.external.Name
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.ApplicantDetailsService._
import services.mocks.MockVatRegistrationService
import testHelpers.VatRegSpec
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

class ApplicantDetailsServiceSpec extends VatRegSpec with ApplicantDetailsFixtures with MockVatRegistrationService with MockRegistrationApiConnector {
  override val testRegId = "testRegId"

  override implicit val currentProfile: CurrentProfile = CurrentProfile(testRegId, VatRegStatus.draft)

  val validFullApplicantDetailsNoFormerName: ApplicantDetails = completeApplicantDetails.copy(
    changeOfName = FormerName(
      hasFormerName = Some(false),
      name = None,
      change = None
    )
  )

  implicit val request: Request[_] = FakeRequest()

  class Setup(backendData: Option[ApplicantDetails] = None) {
    val service = new ApplicantDetailsService(
      mockRegistrationApiConnector,
      vatRegistrationServiceMock
    )

    mockGetSection[ApplicantDetails](testRegId, backendData)
  }

  class SetupForBackendSave(applicantDetails: ApplicantDetails = emptyApplicantDetails) {
    val service: ApplicantDetailsService = new ApplicantDetailsService(
      mockRegistrationApiConnector,
      vatRegistrationServiceMock
    ) {
      override def getApplicantDetails(implicit cp: CurrentProfile, hc: HeaderCarrier, req: Request[_]): Future[ApplicantDetails] = {
        Future.successful(applicantDetails)
      }
    }
  }

  "Calling getApplicantDetails" should {
    "return a full ApplicantDetails view model from backend" in new Setup(Some(completeApplicantDetails)) {
      mockPartyType(Future.successful(UkCompany))
      service.getApplicantDetails returns completeApplicantDetails
    }
  }

  "Calling getApplicantNameForTransactorFlow" should {
    "return firstName from the backend" in new Setup(Some(completeApplicantDetails)) {
      mockIsTransactor(Future.successful(true))
      service.getApplicantNameForTransactorFlow returns Some(testFirstName)
    }
  }

  "Calling getCompanyName" should {
    "return company name from the backend" in new Setup(Some(completeApplicantDetails)) {
      mockPartyType(Future.successful(UkCompany))
      service.getCompanyName returns Some(testCompanyName)
    }
  }

  "Calling getDateOfIncorporation" should {
    "return date of incorp from the backend" in new Setup(Some(completeApplicantDetails)) {
      mockPartyType(Future.successful(UkCompany))
      service.getDateOfIncorporation returns Some(testIncorpDate)
    }
  }

  "Calling updateApplicantDetails" should {
    "return a ApplicantDetails" when {
      "updating current address" in new SetupForBackendSave(completeApplicantDetails) {
        val applicantHomeAddress: Address = Address(line1 = "Line1", line2 = Some("Line2"), postcode = Some("PO BOX"), addressValidated = true)

        val expected: ApplicantDetails = completeApplicantDetails.copy(currentAddress = Some(applicantHomeAddress))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(CurrentAddress(applicantHomeAddress)) returns expected
      }

      "updating business entity details for limited company" in new SetupForBackendSave(completeApplicantDetails.copy(entity = None)) {
        val expected: ApplicantDetails = completeApplicantDetails
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(testLimitedCompany) returns expected
      }

      "updating business entity details for sole trader" in new SetupForBackendSave(soleTraderApplicantDetails.copy(entity = None)) {
        val expected: ApplicantDetails = soleTraderApplicantDetails.copy(roleInTheBusiness = Some(OwnerProprietor))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(Individual))

        service.saveApplicantDetails(testSoleTrader) returns expected
      }

      "updating applicant contact email" in new SetupForBackendSave(completeApplicantDetails.copy(contact = Contact())) {
        val applicantEmailAddress = "tt@dd.uk"
        val expected: ApplicantDetails = completeApplicantDetails.copy(contact = Contact(Some(applicantEmailAddress)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(EmailAddress(applicantEmailAddress)) returns expected
      }

      "updating applicant contact email verified" in new SetupForBackendSave(completeApplicantDetails.copy(contact = Contact())) {
        val applicantEmailVerified = true
        val expected: ApplicantDetails = completeApplicantDetails.copy(contact = Contact(emailVerified = Some(applicantEmailVerified)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(EmailVerified(applicantEmailVerified)) returns expected
      }

      "updating applicant contact telephone number" in new SetupForBackendSave(completeApplicantDetails.copy(contact = Contact())) {
        val applicantTelephoneNumber = "1234"
        val expected: ApplicantDetails = completeApplicantDetails.copy(contact = Contact(tel = Some(applicantTelephoneNumber)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(TelephoneNumber(applicantTelephoneNumber)) returns expected
      }

      "updating applicant has former name" in new SetupForBackendSave(completeApplicantDetails) {
        val expected: ApplicantDetails = completeApplicantDetails.copy(changeOfName = FormerName(hasFormerName = Some(false)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(HasFormerName(false)) returns expected
      }

      "updating applicant former name" in new SetupForBackendSave(completeApplicantDetails) {
        val formerName = Name(Some(testFirstName), last = testLastName)
        val expected: ApplicantDetails = completeApplicantDetails.copy(changeOfName = completeApplicantDetails.changeOfName.copy(name = Some(formerName)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(formerName) returns expected
      }

      "updating applicant former name date change" in new SetupForBackendSave(completeApplicantDetails) {
        val formerNameDate = LocalDate.of(2002, 5, 15)
        val expected: ApplicantDetails = completeApplicantDetails.copy(changeOfName = completeApplicantDetails.changeOfName.copy(change = Some(formerNameDate)))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(formerNameDate) returns expected
      }

      "updating applicant previous address" in new SetupForBackendSave(completeApplicantDetails) {
        val previousAddress = Address(line1 = "PrevLine1", line2 = Some("PrevLine2"), postcode = Some("PO PRE"), addressValidated = true)
        val expected: ApplicantDetails = completeApplicantDetails.copy(previousAddress = Some(previousAddress))
        mockReplaceSection[ApplicantDetails](testRegId, expected)
        mockPartyType(Future.successful(UkCompany))

        service.saveApplicantDetails(PreviousAddress(previousAddress)) returns expected
      }
    }
  }
}
