
package fixtures

import java.time.LocalDate
import models.api.Address
import models.external.{Applicant, EmailAddress, EmailVerified, Name}
import models.view._
import models.{ApplicantDetails, Director, RoleInTheBusiness, TelephoneNumber, TransactorDetails}
import models.external.incorporatedentityid.{BvPass, IncorporationDetails}

trait ApplicantDetailsFixture {


  def generateApplicant(first: String, middle: Option[String], last: String, role: String) = Applicant(
    name = Name(first = Some(first), middle = middle, last = last),
    role = role
  )

  val applicantDob = LocalDate.of(1998, 7, 12)

  val testRole:RoleInTheBusiness = Director
  val applicantNino = "ZZ987654A"
  val validApplicant: Applicant = generateApplicant("First", Some("Middle"), "Last", "Director")
  val applicantDetailsPreIv = ApplicantDetails(None, None, None, None, None, None)
  val validCurrentAddress = Address(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"), addressValidated = true)
  val validPrevAddress = Address(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"), addressValidated = true)
  val applicantEmail = "test@test"
  val testApplicantPhone = "1234"
  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testApplicantNino = "AB123456C"
  val testApplicantDob = LocalDate.of(2020, 1, 1)

  val testTransactorDetails = TransactorDetails(testFirstName, testLastName, testApplicantNino, testApplicantDob)

  val testApplicantCrn = "testCrn"
  val testApplicantCompanyName = "testCompanyName"
  val testApplicantCtUtr = "testCtUtr"
  val testApplicantIncorpDate = LocalDate.of(2020, 2, 3)
  val testBpSafeId = "testBpId"

  val testApplicantIncorpDetails = IncorporationDetails(testApplicantCrn, testApplicantCompanyName, testApplicantCtUtr, testApplicantIncorpDate, "GB", identifiersMatch = true, Some("REGISTERED"), Some(BvPass), Some(testBpSafeId))

  val validFullApplicantDetails = ApplicantDetails(
    transactor = Some(testTransactorDetails),
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    emailAddress = Some(EmailAddress("test@t.test")),
    emailVerified = Some(EmailVerified(true)),
    telephoneNumber = Some(TelephoneNumber(testApplicantPhone)),
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(false, Some(validPrevAddress))),
    entity = Some(testApplicantIncorpDetails),
    roleInTheBusiness = Some(testRole)
  )
}
