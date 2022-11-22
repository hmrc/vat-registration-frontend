
package controllers

import featureswitch.core.config.{OtherBusinessInvolvement => OBI_FS}
import itutil.ControllerISpec
import models._
import models.api._
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import models.view.PreviousAddressView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.Format
import play.api.test.Helpers._

import java.time.LocalDate
import scala.collection.JavaConverters._

class TaskListControllerISpec extends ControllerISpec {

  val url = "/application-progress"

  object ExpectedMessages {
    val section1 = new {
      val heading = "1. Check before you start"
      val row1 = "Registration reason Completed"
    }

    val section1a = new {
      val heading = "2. About you"
      val transactorPersonalDetailsCompletedRow = "Your personal information Completed"
    }

    val section2 = new {
      val heading = "2. Verify the business"
      val heading2 = "3. Verify the business"
      val row1 = "Business information Completed"
    }

    val section3 = new {
      val heading = "3. About you"
      val heading2 = "4. About the business contact"

      val leadPartnerCompletedRow = "Lead partner details Completed"
      val leadPartnerNotStartedRow = "Lead partner details Not started"

      val addressesCompletedRow = "Your addresses Completed"
      val transactorAddressesCompletedRow = "Your addresses Completed"
      val applicantAddressesCompletedRow = "Their addresses Completed"

      val addressesCannotStartRow = "Your addresses Cannot start yet"

      val contactDetailsCompletedRow = "Your contact details Completed"
      val transactorContactDetailsCompletedRow = "Your contact details Completed"
      val applicantContactDetailsCompletedRow = "Their contact details Completed"

      val contactDetailsCannotStartRow = "Your contact details Cannot start yet"

      val personalDetailsCompletedRow = "Your personal information Completed"
      val applicantPersonalDetailsCompletedRow = "Their personal information Completed"
    }

    val aboutTheBusinessSection = new {
      val heading = "About the business"

      val businessDetailsNotStartedRow = "Business details Not started"
      val businessDetailsCompletedRow = "Business details Completed"
      val businessActivitiesCannotStartYetRow = "Business activities Cannot start yet"
      val businessActivitiesNotStartedRow = "Business activities Not started"
      val businessActivitiesCompletedRow = "Business activities Completed"
      val otherBusinessInvolvementsCannotStartYetRow = "Other business involvements Cannot start yet"
      val otherBusinessInvolvementsNotStartedRow = "Other business involvements Not started"
    }

    val vatRegistrationSection = new {
      val heading = "VAT registration"

      val goodsAndServicesCompletedRow = "Goods and services Completed"
      val goodsAndServicesNotStartedRow = "Goods and services Not started"
      val goodsAndServicesCannotStartYetRow = "Goods and services Cannot start yet"

      val bankAccountDetailsCannotStartYetRow = "Bank account details Cannot start yet"
      val bankAccountDetailsNotStartedRow = "Bank account details Not started"
      val bankAccountDetailsCompletedRow = "Bank account details Completed"

      val registrationDateCompletedRow = "Registration date Completed"
      val registrationDateNotStartedRow = "Registration date Not started"
      val registrationDateCannotStartYetRow = "Registration date Cannot start yet"

      val vatReturnsCompletedRow = "VAT returns Completed"
      val vatReturnsNotStartedRow = "VAT returns Not started"
      val vatReturnsInProgress = "VAT returns In progress"
      val vatReturnsCannotStartYetRow = "VAT returns Cannot start yet"

      val frsCannotStartYetRow = "Flat Rate Scheme Cannot start yet"
      val frsCompletedRow = "Flat Rate Scheme Completed"
    }
  }

  def sectionMustExist(n: Int)(heading: String, rows: List[String])(implicit doc: Document) = {
    val sectionHeadingSelector = ".app-task-list__section"
    val rowSelector = "ul.app-task-list__items"

    doc.select(sectionHeadingSelector).eachText().asScala.toList.lift(n - 1) mustBe Some(heading)
    val items = doc.select(rowSelector).asScala.toList.apply(n - 1)
    items.select("li").eachText().asScala.toList mustBe rows
  }

  "GET /application-progress" when {
    "the TaskList feature switch is enabled" must {
      "return OK and render all relevant rows when all data is present" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership)),
          applicantDetails = Some(validFullApplicantDetails),
          entities = Some(List(Entity(Some(testSoleTrader), Partnership, isLeadPartner = Some(true), None, None, None, None)))
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.leadPartnerCompletedRow,
          ExpectedMessages.section3.personalDetailsCompletedRow,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
      }

      "return OK and not render lead partner details row for non-partnership party type even when all data is present in the BE" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData),
          applicantDetails = Some(validFullApplicantDetails),
          entities = Some(List(Entity(Some(testSoleTrader), Individual, isLeadPartner = Some(true), None, None, None, None)))
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](None)
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.personalDetailsCompletedRow,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
      }

      "show the transactor section when all data is present" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(validFullApplicantDetails)
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section1a.heading, List(
          ExpectedMessages.section1a.transactorPersonalDetailsCompletedRow,
          ExpectedMessages.section3.transactorAddressesCompletedRow,
          ExpectedMessages.section3.transactorContactDetailsCompletedRow
        ))
        sectionMustExist(3)(ExpectedMessages.section2.heading2, List(ExpectedMessages.section2.row1))
        sectionMustExist(4)(ExpectedMessages.section3.heading2, List(
          ExpectedMessages.section3.applicantPersonalDetailsCompletedRow,
          ExpectedMessages.section3.applicantAddressesCompletedRow,
          ExpectedMessages.section3.applicantContactDetailsCompletedRow
        ))
      }

      "show the transactor section without address tasklist when in agent flow" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(validFullApplicantDetails)
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised(arn = Some(testArn))
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section1a.heading, List(
          ExpectedMessages.section1a.transactorPersonalDetailsCompletedRow,
          ExpectedMessages.section3.transactorContactDetailsCompletedRow
        ))
        sectionMustExist(3)(ExpectedMessages.section2.heading2, List(ExpectedMessages.section2.row1))
        sectionMustExist(4)(ExpectedMessages.section3.heading2, List(
          ExpectedMessages.section3.applicantPersonalDetailsCompletedRow,
          ExpectedMessages.section3.applicantAddressesCompletedRow,
          ExpectedMessages.section3.applicantContactDetailsCompletedRow
        ))
      }

      "show the lead partner section when the user is a partnership" in new Setup {
        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership)),
          applicantDetails = Some(validFullApplicantDetails)
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(Partnership)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = Partnership)))
          .registrationApi.getSection[ApplicantDetails](
          Some(validFullApplicantDetails.copy(
            entity = Some(testPartnership), personalDetails = None,
            homeAddress = None,
            previousAddress = Some(PreviousAddressView(false, None)),
            emailAddress = None, emailVerified = None, telephoneNumber = None
          ))
        )
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.leadPartnerNotStartedRow,
          "Your personal information Cannot start yet",
          ExpectedMessages.section3.addressesCannotStartRow,
          ExpectedMessages.section3.contactDetailsCannotStartRow
        ))
      }

      "show business activities section with correct states when pre-requisites are not complete" in new Setup {
        enable(OBI_FS)

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(validFullApplicantDetails)
        )
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)


        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(5)(s"5. ${ExpectedMessages.aboutTheBusinessSection.heading}", List(
          ExpectedMessages.aboutTheBusinessSection.businessDetailsNotStartedRow,
          ExpectedMessages.aboutTheBusinessSection.businessActivitiesCannotStartYetRow,
          ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsCannotStartYetRow
        ))
        disable(OBI_FS)
      }

      "show business activities section with business details section complete" in new Setup {
        enable(OBI_FS)

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = fullVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          business = Some(businessDetails.copy(
            hasWebsite = Some(true), businessActivities = None, mainBusinessActivity = None, businessDescription = None
          ))
        )
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(5)(s"5. ${ExpectedMessages.aboutTheBusinessSection.heading}", List(
          ExpectedMessages.aboutTheBusinessSection.businessDetailsCompletedRow,
          ExpectedMessages.aboutTheBusinessSection.businessActivitiesNotStartedRow,
          ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsCannotStartYetRow
        ))
        disable(OBI_FS)
      }

      "show business activities section with business details and activities section complete" in new Setup {
        enable(OBI_FS)

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = fullVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          business = Some(businessDetails.copy(
            hasWebsite = Some(true), hasLandAndProperty = Some(false)
          ))
        )
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(5)(s"5. ${ExpectedMessages.aboutTheBusinessSection.heading}", List(
          ExpectedMessages.aboutTheBusinessSection.businessDetailsCompletedRow,
          ExpectedMessages.aboutTheBusinessSection.businessActivitiesCompletedRow,
          ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsNotStartedRow
        ))
        disable(OBI_FS)
      }

      "show vat registration section with correct states when pre-requisites are not met" in new Setup {
        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          applicantDetails = Some(validFullApplicantDetails)
        )

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[VatApplication](scheme.vatApplication)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(6)(s"6. ${ExpectedMessages.vatRegistrationSection.heading}", List(
          ExpectedMessages.vatRegistrationSection.goodsAndServicesCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.bankAccountDetailsCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.registrationDateCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.frsCannotStartYetRow
        ))
      }

      "show vat registration section when pre-requisites met but no registration tasks started" in new Setup {
        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = fullVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
          business = Some(businessDetails.copy(
            hasWebsite = Some(true), hasLandAndProperty = Some(false), otherBusinessInvolvement = Some(false)
          )),
          vatApplication = None,
          bankAccount = None
        )

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[VatApplication](scheme.vatApplication)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(6)(s"6. ${ExpectedMessages.vatRegistrationSection.heading}", List(
          ExpectedMessages.vatRegistrationSection.goodsAndServicesNotStartedRow,
          ExpectedMessages.vatRegistrationSection.bankAccountDetailsCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.registrationDateCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.frsCannotStartYetRow
        ))
      }

      "show vat registration section when pre-requisites and all tasks completed" in new Setup {
        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        val scheme = fullVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData),
          business = Some(businessDetails.copy(
            hasWebsite = Some(true), hasLandAndProperty = Some(false), otherBusinessInvolvement = Some(false)
          )),
          vatApplication = Some(fullVatApplication.copy(
            overseasCompliance = Some(OverseasCompliance(
              goodsToOverseas = Some(false),
              storingGoodsForDispatch = Some(StoringOverseas)
            )),
            northernIrelandProtocol = Some(NIPTurnover(
              goodsToEU = Some(ConditionalValue(answer = false, None)),
              goodsFromEU = Some(ConditionalValue(answer = false, None))
            )),
            startDate = Some(LocalDate.of(2017, 10, 10)),
            hasTaxRepresentative = Some(false)
          )),
          bankAccount = Some(bankAccount.copy(isProvided = false, None, Some(BeingSetupOrNameChange)))
        )

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[Business](scheme.business)
          .registrationApi.getSection[VatApplication](scheme.vatApplication)
          .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))
          .attachmentsApi.getAttachments(attachments = List(IdentityEvidence))
          .attachmentsApi.getIncompleteAttachments(attachments = List.empty)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(5)(s"5. ${ExpectedMessages.vatRegistrationSection.heading}", List(
          ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
          ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
          ExpectedMessages.vatRegistrationSection.registrationDateCompletedRow,
          ExpectedMessages.vatRegistrationSection.vatReturnsCompletedRow,
          ExpectedMessages.vatRegistrationSection.frsCompletedRow
        ))
      }
    }
  }
}
