/*
 * Copyright 2022 HM Revenue & Customs
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

import featureswitch.core.config.FeatureSwitching
import models._
import models.api.{Address, NETP, NonUkNonEstablished, PartyType, VatScheme}
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class TransactorDetailsBlockBuilder @Inject()() extends FeatureSwitching {

  val presentationFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val sectionId: String = "cya.transactor"

  def generateTransactorSummaryList(implicit vatScheme: VatScheme, messages: Messages): SummaryList = {
    val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType)
      .getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing party type"))
    val optTransactorDetails: Option[TransactorDetails] = vatScheme.transactorDetails
    val summaryListRows: Seq[SummaryListRow] = optTransactorDetails.fold(Seq[SummaryListRow]())(transactorDetails =>
      generateTransactorSummaryListRows(transactorDetails, partyType)
    )

    SummaryList(
      summaryListRows
    )
  }

  private def generateTransactorSummaryListRows(transactorDetails: TransactorDetails,
                                                partyType: PartyType)
                                               (implicit messages: Messages): Seq[SummaryListRow] = {

    val isAgent = transactorDetails.personalDetails.exists(_.arn.nonEmpty)
    val isPartOfOrganisation = optSummaryListRowBoolean(
      s"$sectionId.isPartOfOrganisation",
      transactorDetails.isPartOfOrganisation,
      Some(controllers.registration.transactor.routes.PartOfOrganisationController.show.url)
    )

    val organisationName = optSummaryListRowString(
      s"$sectionId.organisationName",
      transactorDetails.organisationName,
      Some(controllers.registration.transactor.routes.OrganisationNameController.show.url)
    )

    val roleInTheBusiness = optSummaryListRowString(
      s"$sectionId.roleInTheBusiness",
      transactorDetails.declarationCapacity.flatMap {
        case DeclarationCapacityAnswer(AccountantAgent, _) if isAgent => None
        case DeclarationCapacityAnswer(AccountantAgent, _) => Some("declarationCapacity.accountant")
        case DeclarationCapacityAnswer(Representative, _) => Some("declarationCapacity.representative")
        case DeclarationCapacityAnswer(BoardMember, _) => Some("declarationCapacity.boardMember")
        case DeclarationCapacityAnswer(AuthorisedEmployee, _) => Some("declarationCapacity.authorisedEmployee")
        case DeclarationCapacityAnswer(Other, role) => role
      },
      Some(controllers.registration.transactor.routes.DeclarationCapacityController.show.url)
    )

    val fullName = optSummaryListRowString(
      s"$sectionId.fullName",
      transactorDetails.personalDetails.map(details => s"${details.firstName} ${details.lastName}"),
      if (isAgent) {
        Some(controllers.registration.transactor.routes.AgentNameController.show.url)
      } else {
        Some(controllers.registration.transactor.routes.TransactorIdentificationController.startJourney.url)
      }
    )

    val dateOfBirth = optSummaryListRowString(
      s"$sectionId.dateOfBirth",
      transactorDetails.personalDetails.flatMap(_.dateOfBirth.map(_.format(presentationFormatter))),
      Some(controllers.registration.transactor.routes.TransactorIdentificationController.startJourney.url)
    )

    val nino = optSummaryListRowString(
      s"$sectionId.nino",
      transactorDetails.personalDetails.flatMap(_.nino),
      Some(controllers.registration.transactor.routes.TransactorIdentificationController.startJourney.url)
    )

    val homeAddress = optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      transactorDetails.address.map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
          Some(controllers.registration.transactor.routes.TransactorInternationalAddressController.show.url)
        case _ =>
          Some(controllers.registration.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
      }
    )

    val telephoneNumber = optSummaryListRowString(
      s"$sectionId.telephoneNumber",
      transactorDetails.telephone,
      Some(controllers.registration.transactor.routes.TelephoneNumberController.show.url)
    )

    val emailAddress = optSummaryListRowString(
      s"$sectionId.emailAddress",
      transactorDetails.email,
      Some(controllers.registration.transactor.routes.TransactorCaptureEmailAddressController.show.url)
    )

    Seq(
      isPartOfOrganisation,
      organisationName,
      roleInTheBusiness,
      fullName,
      dateOfBirth,
      nino,
      homeAddress,
      telephoneNumber,
      emailAddress
    ).flatten
  }
}
