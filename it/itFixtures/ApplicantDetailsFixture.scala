
package itFixtures

import models._
import models.api.Address
import models.external._

import java.time.LocalDate

trait ApplicantDetailsFixture {

  val applicantDob: LocalDate = LocalDate.of(1998, 7, 12)

  val testRole: RoleInTheBusiness = Director
  val applicantNino = "ZZ987654A"
  val validCurrentAddress: Address = Address(line1 = "Test Line1", line2 = Some("Test Line2"), postcode = Some("TE 1ST"), addressValidated = true)
  val validPrevAddress: Address = Address(line1 = "Test Line11", line2 = Some("Test Line22"), postcode = Some("TE1 1ST"), addressValidated = true)
  val applicantEmail = "test@test"
  val testApplicantPhone = "1234"
  val testFirstName = "testFirstName"
  val testLastName = "testLastName"
  val testFormerFirstName = "New"
  val testFormerMiddleName = "Name"
  val testFormerLastName = "Cosmo"
  val testApplicantNino = "AB123456C"
  val testTrn = "0001234567"
  val testApplicantDob: LocalDate = LocalDate.of(2020, 1, 1)

  val testPersonalDetails: PersonalDetails = PersonalDetails(testFirstName, testLastName, Some(testApplicantNino), None, identifiersMatch = true, Some(testApplicantDob))
  val testPersonalDetailsArn: PersonalDetails = PersonalDetails(testFirstName, testLastName, Some(testApplicantNino), None, identifiersMatch = true, Some(testApplicantDob), arn = Some("test arn"))
  val testNetpPersonalDetails: PersonalDetails = PersonalDetails(testFirstName, testLastName, None, Some(testTrn), identifiersMatch = false, Some(testApplicantDob))

  val testApplicantCrn = "testCrn"
  val testApplicantCompanyName = "testCompanyName"
  val testApplicantCtUtr = "testCtUtr"
  val testApplicantIncorpDate: Option[LocalDate] = Some(LocalDate.of(2020, 2, 3))
  val testBpSafeId = "testBpId"

  val testApplicantIncorpDetails: IncorporatedEntity = IncorporatedEntity(testApplicantCrn, Some(testApplicantCompanyName), Some(testApplicantCtUtr), None, testApplicantIncorpDate, "GB", identifiersMatch = true, RegisteredStatus, Some(BvPass), Some(testBpSafeId))
  val testMinorEntity: MinorEntity = MinorEntity(None, None, Some(testApplicantCtUtr), None, None, None, None, RegisteredStatus, Some(BvPass), Some(testBpSafeId), identifiersMatch = true)
  val testPartnership: PartnershipIdEntity = PartnershipIdEntity(None, None, None, None, None, RegisteredStatus, Some(BvPass), None, identifiersMatch = true)
  val validFullApplicantDetails: ApplicantDetails = ApplicantDetails(
    personalDetails = Some(testPersonalDetails),
    entity = Some(testApplicantIncorpDetails),
    currentAddress = Some(validCurrentAddress),
    noPreviousAddress = Some(false),
    previousAddress = Some(validPrevAddress),
    contact = Contact(
      email = Some("test@t.test"),
      tel = Some(testApplicantPhone),
      emailVerified = Some(true)
    ),
    changeOfName = FormerName(
      hasFormerName = Some(true),
      name = Some(Name(Some(testFormerFirstName), Some(testFormerMiddleName), testFormerLastName)),
      change = Some(LocalDate.of(2000, 7, 12))
    ),
    roleInTheBusiness = Some(testRole)
  )


  val testPartnerShipEntity: PartnershipIdEntity = PartnershipIdEntity(
    sautr = Some("1234567890"),
    companyNumber = Some("123456789"),
    companyName = Some("testCompanyName"),
    dateOfIncorporation = Some(LocalDate.of(2020, 1, 1)),
    postCode = Some("AA11AA"),
    registration = RegisteredStatus,
    businessVerification = Some(BvPass),
    bpSafeId = Some("testBpId"),
    identifiersMatch = true
  )

  val validFullApplicantDetailsPartnership: ApplicantDetails = ApplicantDetails(
    personalDetails = Some(testPersonalDetails),
    entity = Some(testPartnerShipEntity),
    currentAddress = Some(validCurrentAddress),
    noPreviousAddress = Some(false),
    previousAddress = Some(validPrevAddress),
    contact = Contact(
      email = Some("test@t.test"),
      tel = Some(testApplicantPhone),
      emailVerified = Some(true)
    ),
    changeOfName = FormerName(
      hasFormerName = Some(true),
      name = Some(Name(Some(testFormerFirstName), Some(testFormerMiddleName), testFormerLastName)),
      change = Some(LocalDate.of(2000, 7, 12))
    ),
    roleInTheBusiness = Some(testRole)
  )
}
