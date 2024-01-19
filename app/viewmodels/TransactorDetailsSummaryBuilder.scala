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

import featuretoggle.FeatureToggleSupport
import models._
import models.api._
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException
import utils.MessageDateFormat

import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class TransactorDetailsSummaryBuilder @Inject()(govukSummaryList: GovukSummaryList) extends FeatureToggleSupport {

  val sectionId: String = "cya.transactor"

  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val eligibilitySubmissionData = vatScheme.eligibilitySubmissionData.getOrElse(throw new InternalServerException("[TransactorDetailsSummaryBuilder] Missing eligibility data"))
    val optTransactorDetails: Option[TransactorDetails] = vatScheme.transactorDetails
    val summaryListRows: Seq[SummaryListRow] = optTransactorDetails.fold(Seq[SummaryListRow]())(transactorDetails =>
      generateTransactorSummaryListRows(transactorDetails, eligibilitySubmissionData.partyType, eligibilitySubmissionData.fixedEstablishmentInManOrUk)
    )

    govukSummaryList(SummaryList(
      summaryListRows
    ))
  }

  private def generateTransactorSummaryListRows(transactorDetails: TransactorDetails,
                                                partyType: PartyType,
                                                fixedEstablishment: Boolean)
                                               (implicit messages: Messages): Seq[SummaryListRow] = {

    val isAgent = transactorDetails.personalDetails.exists(_.arn.nonEmpty)
    val isPartOfOrganisation = optSummaryListRowBoolean(
      s"$sectionId.isPartOfOrganisation",
      transactorDetails.isPartOfOrganisation,
      Some(controllers.transactor.routes.PartOfOrganisationController.show.url)
    )

    val organisationName = optSummaryListRowString(
      s"$sectionId.organisationName",
      transactorDetails.organisationName,
      Some(controllers.transactor.routes.OrganisationNameController.show.url)
    )

    val roleInTheBusiness = optSummaryListRowString(
      s"$sectionId.roleInTheBusiness",
      transactorDetails.declarationCapacity.flatMap {
        case DeclarationCapacityAnswer(AccountantAgent, _) if isAgent => None
        case DeclarationCapacityAnswer(AccountantAgent, _) => Some("declarationCapacity.accountant")
        case DeclarationCapacityAnswer(Representative, _) => Some("declarationCapacity.representative")
        case DeclarationCapacityAnswer(BoardMember, _) => Some("declarationCapacity.boardMember")
        case DeclarationCapacityAnswer(AuthorisedEmployee, _) => Some("declarationCapacity.authorisedEmployee")
        case DeclarationCapacityAnswer(OtherDeclarationCapacity, role) => role
        case answer => throw new IllegalStateException("Invalid declaration capacity answer:" + answer)
      },
      Some(controllers.transactor.routes.DeclarationCapacityController.show.url)
    )

    val fullName = optSummaryListRowString(
      s"$sectionId.fullName",
      transactorDetails.personalDetails.map(details => s"${details.firstName} ${details.lastName}"),
      if (isAgent) {
        Some(controllers.transactor.routes.AgentNameController.show.url)
      } else {
        Some(controllers.grs.routes.TransactorIdController.startJourney.url)
      }
    )

    val dateOfBirth = optSummaryListRowString(
      s"$sectionId.dateOfBirth",
      transactorDetails.personalDetails.flatMap(_.dateOfBirth).map(MessageDateFormat.format),
      Some(controllers.grs.routes.TransactorIdController.startJourney.url)
    )

    val nino = optSummaryListRowString(
      s"$sectionId.nino",
      transactorDetails.personalDetails.flatMap(_.nino),
      Some(controllers.grs.routes.TransactorIdController.startJourney.url)
    )

    val homeAddress = optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      transactorDetails.address.map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished if !fixedEstablishment =>
          Some(controllers.transactor.routes.TransactorInternationalAddressController.show.url)
        case _ =>
          Some(controllers.transactor.routes.TransactorHomeAddressController.redirectToAlf.url)
      }
    )

    val telephoneNumber = optSummaryListRowString(
      s"$sectionId.telephoneNumber",
      transactorDetails.telephone,
      Some(controllers.transactor.routes.TelephoneNumberController.show.url)
    )

    val emailAddress = optSummaryListRowString(
      s"$sectionId.emailAddress",
      transactorDetails.email,
      Some(controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url)
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
