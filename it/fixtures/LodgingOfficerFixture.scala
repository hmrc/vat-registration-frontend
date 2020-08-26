package fixtures

import java.time.LocalDate

import models.api.ScrsAddress
import models.external.{Name, Officer}
import models.view._

trait LodgingOfficerFixture {


  def generateOfficer(first: String, middle: Option[String], last: String, role: String) = Officer(
    name = Name(forename = Some(first), otherForenames = middle, surname = last),
    role = role
  )

  val officerDob = LocalDate.of(1998, 7, 12)

  val officerNino = "ZZ987654A"

  val validOfficer: Officer = generateOfficer("First", Some("Middle"), "Last", "Director")

  val lodgingOfficerPreIv = LodgingOfficer(None, None, None, None, None)

  val validCurrentAddress = ScrsAddress(line1 = "TestLine1", line2 = "TestLine2", postcode = Some("TE 1ST"))

  val validPrevAddress = ScrsAddress(line1 = "TestLine11", line2 = "TestLine22", postcode = Some("TE1 1ST"))

  val officerEmail = "test@test"

  val validFullLodgingOfficer = LodgingOfficer(
    homeAddress = Some(HomeAddressView(validCurrentAddress.id, Some(validCurrentAddress))),
    contactDetails = Some(ContactDetailsView(Some(officerEmail), Some("1234"), Some("5678"))),
    formerName = Some(FormerNameView(true, Some("New Name Cosmo"))),
    formerNameDate = Some(FormerNameDateView(LocalDate.of(2000, 7, 12))),
    previousAddress = Some(PreviousAddressView(true, Some(validPrevAddress)))
  )
}
