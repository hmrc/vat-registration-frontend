/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels

import config.FrontendAppConfig
import models.api._
import models.view.SummaryListRowUtils.{optSummaryListRowSeq, optSummaryListRowString}
import models._
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.MessageDateFormat

import java.time.LocalDate

class TransactorDetailsSummaryBuilderSpec extends VatRegSpec {
  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    object Builder extends TransactorDetailsSummaryBuilder(govukSummaryList)
  }

  val testOrganisationName = "testOrganisationName"
  val testTelephoneNumber = "testTelephoneNumber"
  val testEmail = "testEmail"
  val testOtherDeclarationCapacity = "testOtherDeclarationCapacity"
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(20)
  val testArn = "testArn"

  val testTransactorDetails: TransactorDetails = TransactorDetails(
    personalDetails = Some(PersonalDetails(
      firstName = testFirstName,
      lastName = testLastName,
      nino = Some(testNino),
      trn = None,
      identifiersMatch = true,
      dateOfBirth = Some(testDateOfBirth),
      arn = None
    )),
    isPartOfOrganisation = Some(true),
    organisationName = Some(testOrganisationName),
    telephone = Some(testTelephoneNumber),
    email = Some(testEmail),
    emailVerified = Some(true),
    address = Some(testAddress),
    declarationCapacity = Some(DeclarationCapacityAnswer(OtherDeclarationCapacity, Some(testOtherDeclarationCapacity)))
  )

  object TestContent {
    val isPartOfOrganisation = "Part of an organisation"
    val organisationName = "Organisation name"
    val roleInTheBusiness = "Role with the business"
    val fullName = "Full name"
    val dateOfBirth = "Date of birth"
    val nino = "National Insurance number"
    val homeAddress = "Home address"
    val telephoneNumber = "Telephone number"
    val emailAddress = "Email address"
  }

  "generateTransactorSummaryList" when {
    "return an full summary list for a non-Overseas transactor" in new Setup {
      val testVatScheme: VatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = UkCompany,
          isTransactor = true
        )),
        transactorDetails = Some(testTransactorDetails)
      )

      val expectedSummaryList: SummaryList = SummaryList(
        Seq(
          optSummaryListRowString(
            TestContent.isPartOfOrganisation,
            Some("Yes"),
            Some(controllers.transactor.routes.PartOfOrganisationController.show.url)
          ),
          optSummaryListRowString(
            TestContent.organisationName,
            Some(testOrganisationName),
            Some(controllers.transactor.routes.OrganisationNameController.show.url)
          ),
          optSummaryListRowString(
            TestContent.roleInTheBusiness,
            Some(testOtherDeclarationCapacity),
            Some(controllers.transactor.routes.DeclarationCapacityController.show.url)
          ),
          optSummaryListRowString(
            TestContent.fullName,
            Some(testFirstName + " " + testLastName),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowString(
            TestContent.dateOfBirth,
            Some(MessageDateFormat.format(testDateOfBirth)),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowString(
            TestContent.nino,
            Some(testNino),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowSeq(
            TestContent.homeAddress,
            Some(Address.normalisedSeq(testAddress)),
            Some(controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
          ),
          optSummaryListRowString(
            TestContent.telephoneNumber,
            Some(testTelephoneNumber),
            Some(controllers.transactor.routes.TelephoneNumberController.show.url)
          ),
          optSummaryListRowString(
            TestContent.emailAddress,
            Some(testEmail),
            Some(controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url)
          )
        ).flatten
      )

      val res = Builder.build(testVatScheme)(messages)

      res mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "return an full summary list for an Overseas transactor" in new Setup {
      val testVatScheme: VatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = NETP,
          isTransactor = true,
          fixedEstablishmentInManOrUk = false
        )),
        transactorDetails = Some(testTransactorDetails)
      )

      val expectedSummaryList: SummaryList = SummaryList(
        Seq(
          optSummaryListRowString(
            TestContent.isPartOfOrganisation,
            Some("Yes"),
            Some(controllers.transactor.routes.PartOfOrganisationController.show.url)
          ),
          optSummaryListRowString(
            TestContent.organisationName,
            Some(testOrganisationName),
            Some(controllers.transactor.routes.OrganisationNameController.show.url)
          ),
          optSummaryListRowString(
            TestContent.roleInTheBusiness,
            Some(testOtherDeclarationCapacity),
            Some(controllers.transactor.routes.DeclarationCapacityController.show.url)
          ),
          optSummaryListRowString(
            TestContent.fullName,
            Some(testFirstName + " " + testLastName),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowString(
            TestContent.dateOfBirth,
            Some(MessageDateFormat.format(testDateOfBirth)),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowString(
            TestContent.nino,
            Some(testNino),
            Some(controllers.grs.routes.TransactorIdController.startJourney.url)
          ),
          optSummaryListRowSeq(
            TestContent.homeAddress,
            Some(Address.normalisedSeq(testAddress)),
            Some(controllers.transactor.routes.TransactorInternationalAddressController.show.url)
          ),
          optSummaryListRowString(
            TestContent.telephoneNumber,
            Some(testTelephoneNumber),
            Some(controllers.transactor.routes.TelephoneNumberController.show.url)
          ),
          optSummaryListRowString(
            TestContent.emailAddress,
            Some(testEmail),
            Some(controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url)
          )
        ).flatten
      )

      val res = Builder.build(testVatScheme)(messages)

      res mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "return an full summary list for an Agent transactor" in new Setup {
      val testVatScheme: VatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = UkCompany,
          isTransactor = true
        )),
        transactorDetails = Some(TransactorDetails(
          personalDetails = Some(PersonalDetails(
            firstName = testFirstName,
            lastName = testLastName,
            nino = None,
            trn = None,
            identifiersMatch = true,
            dateOfBirth = None,
            arn = Some(testArn)
          )),
          isPartOfOrganisation = None,
          organisationName = None,
          telephone = Some(testTelephoneNumber),
          email = Some(testEmail),
          emailVerified = Some(true),
          address = None,
          declarationCapacity = Some(DeclarationCapacityAnswer(AccountantAgent, None))
        ))
      )

      val expectedSummaryList: SummaryList = SummaryList(
        Seq(
          optSummaryListRowString(
            TestContent.fullName,
            Some(testFirstName + " " + testLastName),
            Some(controllers.transactor.routes.AgentNameController.show.url)
          ),
          optSummaryListRowString(
            TestContent.telephoneNumber,
            Some(testTelephoneNumber),
            Some(controllers.transactor.routes.TelephoneNumberController.show.url)
          ),
          optSummaryListRowString(
            TestContent.emailAddress,
            Some(testEmail),
            Some(controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url)
          )
        ).flatten
      )

      val res = Builder.build(testVatScheme)(messages)

      res mustBe HtmlFormat.fill(List(govukSummaryList(expectedSummaryList)))
    }

    "return an empty summary list for a non-transactor" in new Setup {
      val testVatScheme: VatScheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData.copy(
          partyType = UkCompany,
          isTransactor = false
        )),
        transactorDetails = None
      )

      val res = Builder.build(testVatScheme)(messages)

      res mustBe HtmlFormat.fill(List(govukSummaryList(SummaryList(Nil))))
    }
  }
}
