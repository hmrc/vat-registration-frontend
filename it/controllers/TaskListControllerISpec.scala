
package controllers

import featureswitch.core.config.TaskList
import itutil.ControllerISpec
import models.api.vatapplication.{OverseasCompliance, StoringOverseas, VatApplication}
import models.api._
import models.view.PreviousAddressView
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.Format
import play.api.test.Helpers._
import play.api.libs.json.Json

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
      val row1 = "Personal details Completed"
    }

    val section2 = new {
      val heading = "2. Verify the business"
      val heading2 = "3. Verify the business"
      val row1 = "Business information Completed"
    }

    val section3 = new {
      val heading = "3. About you"

      val leadPartnerCompletedRow = "Lead partner details Completed"
      val leadPartnerNotStartedRow = "Lead partner details Not started"
      val addressesCompletedRow = "Addresses Completed"
      val addressesCannotStartRow = "Addresses Cannot start yet"
      val contactDetailsCompletedRow = "Contact details Completed"
      val contactDetailsCannotStartRow = "Contact details Cannot start yet"

      val heading2 = "4. About the business contact"
      val row1 = "Personal details Completed"
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
        enable(TaskList)

        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(partyType = Partnership)),
          applicantDetails = Some(validFullApplicantDetails),
          partners = Some(List(PartnerEntity(testSoleTrader, Partnership, isLeadPartner = true)))
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.leadPartnerCompletedRow,
          ExpectedMessages.section3.row1,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
      }

      "return OK and not render lead partner details row for non-partnership party type even when all data is present in the BE" in new Setup {
        enable(TaskList)

        val scheme = emptyUkCompanyVatScheme.copy(
          eligibilitySubmissionData = Some(testEligibilitySubmissionData),
          applicantDetails = Some(validFullApplicantDetails),
          partners = Some(List(PartnerEntity(testSoleTrader, Individual, isLeadPartner = true)))
        )

        implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

        given
          .user.isAuthorised()
          .registrationApi.getRegistration(scheme)
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
          .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
          .registrationApi.getSection[TransactorDetails](None)

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.row1,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
      }

      "show the transactor section when all data is present" in new Setup {
        enable(TaskList)

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

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section1a.heading, List(
          ExpectedMessages.section1a.row1,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
        sectionMustExist(3)(ExpectedMessages.section2.heading2, List(ExpectedMessages.section2.row1))
        sectionMustExist(4)(ExpectedMessages.section3.heading2, List(
          ExpectedMessages.section3.row1,
          ExpectedMessages.section3.addressesCompletedRow,
          ExpectedMessages.section3.contactDetailsCompletedRow
        ))
      }

      "show the lead partner section when the user is a partnership" in new Setup {
        enable(TaskList)

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

        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        implicit val doc = Jsoup.parse(res.body)

        res.status mustBe OK
        sectionMustExist(1)(ExpectedMessages.section1.heading, List(ExpectedMessages.section1.row1))
        sectionMustExist(2)(ExpectedMessages.section2.heading, List(ExpectedMessages.section2.row1))
        sectionMustExist(3)(ExpectedMessages.section3.heading, List(
          ExpectedMessages.section3.leadPartnerNotStartedRow,
          "Personal details Cannot start yet",
          ExpectedMessages.section3.addressesCannotStartRow,
          ExpectedMessages.section3.contactDetailsCannotStartRow
        ))
      }

      "show business activities section with correct states" in new Setup {
        enable(TaskList)

        private def verifyAboutTheBusinessTaskListSection(position: Int, scheme: VatScheme, heading: String, expectedRows: List[String]) = {
          implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

          given
            .user.isAuthorised()
            .registrationApi.getRegistration(scheme)
            .registrationApi.getSection[Business](scheme.business)
            .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
            .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
            .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)
          implicit val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          sectionMustExist(position)(s"$position. $heading", expectedRows)
        }

        verifyAboutTheBusinessTaskListSection(
          5,
          emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
            applicantDetails = Some(validFullApplicantDetails)
          ),
          ExpectedMessages.aboutTheBusinessSection.heading, List(
            ExpectedMessages.aboutTheBusinessSection.businessDetailsNotStartedRow,
            ExpectedMessages.aboutTheBusinessSection.businessActivitiesCannotStartYetRow,
            ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsCannotStartYetRow
          )
        )

        verifyAboutTheBusinessTaskListSection(
          5,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
            business = Some(businessDetails.copy(
              hasWebsite = Some(true), businessActivities = None, mainBusinessActivity = None, businessDescription = None
            ))
          ),
          ExpectedMessages.aboutTheBusinessSection.heading, List(
            ExpectedMessages.aboutTheBusinessSection.businessDetailsCompletedRow,
            ExpectedMessages.aboutTheBusinessSection.businessActivitiesNotStartedRow,
            ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsCannotStartYetRow
          )
        )

        verifyAboutTheBusinessTaskListSection(
          5,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
            business = Some(businessDetails.copy(
              hasWebsite = Some(true), hasLandAndProperty = Some(false)
            ))
          ),
          ExpectedMessages.aboutTheBusinessSection.heading, List(
            ExpectedMessages.aboutTheBusinessSection.businessDetailsCompletedRow,
            ExpectedMessages.aboutTheBusinessSection.businessActivitiesCompletedRow,
            ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsNotStartedRow
          )
        )

        verifyAboutTheBusinessTaskListSection(
          4,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData),
            business = Some(businessDetails.copy(
              hasWebsite = Some(true), hasLandAndProperty = Some(false)
            ))
          ),
          ExpectedMessages.aboutTheBusinessSection.heading, List(
            ExpectedMessages.aboutTheBusinessSection.businessDetailsCompletedRow,
            ExpectedMessages.aboutTheBusinessSection.businessActivitiesCompletedRow,
            ExpectedMessages.aboutTheBusinessSection.otherBusinessInvolvementsNotStartedRow
          )
        )
      }

      "show vat registration section with correct states" in new Setup {
        enable(TaskList)

        private def verifyVatRegistrationTaskListSection(position: Int, scheme: VatScheme, heading: String, expectedRows: List[String]) = {
          implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

          given
            .user.isAuthorised()
            .registrationApi.getRegistration(scheme)
            .registrationApi.getSection[Business](scheme.business)
            .registrationApi.getSection[VatApplication](scheme.vatApplication)
            .registrationApi.getSection[EligibilitySubmissionData](scheme.eligibilitySubmissionData)
            .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
            .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))

          if (scheme.bankAccount.isDefined) given.registrationApi.getSection[BankAccount](scheme.bankAccount)

          insertCurrentProfileIntoDb(currentProfile, sessionId)

          val res = await(buildClient(url).get)
          implicit val doc = Jsoup.parse(res.body)

          res.status mustBe OK
          sectionMustExist(position)(s"$position. $heading", expectedRows)
        }

        verifyVatRegistrationTaskListSection(
          6,
          emptyUkCompanyVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
            applicantDetails = Some(validFullApplicantDetails)
          ),
          ExpectedMessages.vatRegistrationSection.heading,
          List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCannotStartYetRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCannotStartYetRow,
            ExpectedMessages.vatRegistrationSection.registrationDateCannotStartYetRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow
          )
        )

        verifyVatRegistrationTaskListSection(
          6,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
            business = Some(businessDetails.copy(
              hasWebsite = Some(true), hasLandAndProperty = Some(false), otherBusinessInvolvement = Some(false)
            )),
            vatApplication = None,
            bankAccount = None
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesNotStartedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCannotStartYetRow,
            ExpectedMessages.vatRegistrationSection.registrationDateCannotStartYetRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow
          )
        )

        verifyVatRegistrationTaskListSection(
          6,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
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
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              ))
            )),
            bankAccount = Some(bankAccount.copy(
              isProvided = true, Some(testUkBankDetails), None, None
            ))
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
            ExpectedMessages.vatRegistrationSection.registrationDateNotStartedRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow
          )
        )

        verifyVatRegistrationTaskListSection(
          6,
          fullVatScheme.copy(
            eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(
              isTransactor = true, registrationReason = NonUk
            )),
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
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              )),
              returnsFrequency = None,
              staggerStart = None
            )),
            bankAccount = Some(bankAccount.copy(
              isProvided = true, Some(testUkBankDetails), None, None
            ))
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsNotStartedRow
          )
        )

        verifyVatRegistrationTaskListSection(
          5,
          fullVatScheme.copy(
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
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              )),
              startDate = Some(LocalDate.of(2017, 10, 10)),
              returnsFrequency = None,
              staggerStart = None
            )),
            bankAccount = Some(bankAccount.copy(
              isProvided = false, None, None, Some(BeingSetup)
            ))
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
            ExpectedMessages.vatRegistrationSection.registrationDateCompletedRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsNotStartedRow
          )
        )

        verifyVatRegistrationTaskListSection(
          5,
          fullVatScheme.copy(
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
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              )),
              startDate = Some(LocalDate.of(2017, 10, 10)),
              staggerStart = None
            )),
            bankAccount = Some(bankAccount.copy(isProvided = false, None, None, Some(BeingSetup)))
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
            ExpectedMessages.vatRegistrationSection.registrationDateCompletedRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsInProgress
          )
        )

        verifyVatRegistrationTaskListSection(
          5,
          fullVatScheme.copy(
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
                goodsFromEU = Some(ConditionalValue(answer = false, None)),
              )),
              startDate = Some(LocalDate.of(2017, 10, 10)),
              hasTaxRepresentative = Some(false)
            )),
            bankAccount = Some(bankAccount.copy(isProvided = false, None, None, Some(BeingSetup)))
          ),
          ExpectedMessages.vatRegistrationSection.heading, List(
            ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
            ExpectedMessages.vatRegistrationSection.bankAccountDetailsCompletedRow,
            ExpectedMessages.vatRegistrationSection.registrationDateCompletedRow,
            ExpectedMessages.vatRegistrationSection.vatReturnsCompletedRow,
          )
        )
      }
    }

    "show vat registration section with correct state for bank account details flow not started" in new Setup {
      implicit val applicantDetailsFormat: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)

      val scheme = fullVatScheme.copy(
        eligibilitySubmissionData = Some(testEligibilitySubmissionData.copy(isTransactor = true)),
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
            goodsFromEU = Some(ConditionalValue(answer = false, None)),
          ))
        )),
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
        .s4l.contains(BankAccount.s4lKey.key, Json.stringify(Json.obj()))
        .vatScheme.doesNotExistForKey("bank-account")

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).get)
      implicit val doc = Jsoup.parse(res.body)

      res.status mustBe OK
      sectionMustExist(6)(s"6. ${ExpectedMessages.vatRegistrationSection.heading}",
        List(
          ExpectedMessages.vatRegistrationSection.goodsAndServicesCompletedRow,
          ExpectedMessages.vatRegistrationSection.bankAccountDetailsNotStartedRow,
          ExpectedMessages.vatRegistrationSection.registrationDateCannotStartYetRow,
          ExpectedMessages.vatRegistrationSection.vatReturnsCannotStartYetRow
        )
      )
    }

    "the TaskList feature switch is disabled" must {
      "return NOT FOUND" in new Setup {
        disable(TaskList)

        given.user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionId)

        val res = await(buildClient(url).get)
        res.status mustBe NOT_FOUND
      }
    }
  }

}
