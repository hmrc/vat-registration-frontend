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

import controllers.applicant.{routes => applicantRoutes}
import controllers.grs.{routes => grsRoutes}
import featureswitch.core.config.FeatureSwitching
import models.Entity.leadEntityIndex
import models._
import models.api._
import models.external.{IncorporatedEntity, PartnershipIdEntity, SoleTraderIdEntity}
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
class ApplicantDetailsSummaryBuilder @Inject()(govukSummaryList: GovukSummaryList) extends FeatureSwitching {
  val sectionId: String = "cya.applicantDetails"

  def build(vatScheme: VatScheme)(implicit messages: Messages): HtmlFormat.Appendable = {
    val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType)
      .getOrElse(throw new InternalServerException("[ApplicantDetailsSummaryBuilder] Missing party type"))

    govukSummaryList(SummaryList(generateLeadPartnerSummaryListRows(vatScheme) ++ generateApplicantDetailsSummaryListRows(vatScheme, partyType)))
  }

  def generateApplicantDetailsSummaryListRows(vatScheme: VatScheme, partyType: PartyType)(implicit messages: Messages): Seq[SummaryListRow] = {
    val isTransactor = vatScheme.eligibilitySubmissionData.exists(_.isTransactor)
    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw new InternalServerException("[SummaryApplicantDetailsBuilder] Missing applicant details block"))

    val changePersonalDetailsUrl: String = {
      partyType match {
        case Individual | NETP =>
          grsRoutes.SoleTraderIdController.startJourney.url
        case Partnership | ScotPartnership | LtdPartnership | ScotLtdPartnership =>
          vatScheme.entities.flatMap(_.headOption.map(_.partyType)) match {
            case Some(Individual | NETP) => grsRoutes.PartnerSoleTraderIdController.startJourney(leadEntityIndex).url
            case _ => grsRoutes.IndividualIdController.startJourney.url
          }
        case _ =>
          grsRoutes.IndividualIdController.startJourney.url
      }
    }

    val fullName = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.fullName" else s"$sectionId.self.fullName",
      applicantDetails.personalDetails.map(details => s"${details.firstName} ${details.lastName}"),
      Some(changePersonalDetailsUrl)
    )

    val nino = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.nino" else s"$sectionId.self.nino",
      applicantDetails.personalDetails.flatMap(_.nino),
      Some(changePersonalDetailsUrl)
    )

    val dob = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.dob" else s"$sectionId.self.dob",
      for {
        personalDetails <- applicantDetails.personalDetails
        dob <- personalDetails.dateOfBirth
      } yield MessageDateFormat.format(dob),
      Some(changePersonalDetailsUrl)
    )

    val roleInTheBusiness = partyType match {
      case NETP | Individual =>
        None
      case _ =>
        optSummaryListRowString(
          if (isTransactor) s"$sectionId.transactor.roleInTheBusiness" else s"$sectionId.self.roleInTheBusiness",
          applicantDetails.roleInTheBusiness.collect {
            case Director => "pages.roleInTheBusiness.radio1"
            case CompanySecretary => "pages.roleInTheBusiness.radio2"
          },
          Some(applicantRoutes.CaptureRoleInTheBusinessController.show.url)
        )
    }

    val changedName = optSummaryListRowBoolean(
      if (isTransactor) s"$sectionId.transactor.formerName" else s"$sectionId.self.formerName",
      applicantDetails.hasFormerName,
      Some(applicantRoutes.FormerNameController.show.url)
    )

    val formerName = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.formerNameCapture" else s"$sectionId.self.formerNameCapture",
      applicantDetails.formerName.map(_.asLabel),
      Some(applicantRoutes.FormerNameCaptureController.show.url)
    )

    val formerNameDate = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.formerNameDate" else s"$sectionId.self.formerNameDate",
      applicantDetails.formerNameDate.map(_.date).map(MessageDateFormat.format),
      Some(applicantRoutes.FormerNameDateController.show.url)
    )

    val email = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.email" else s"$sectionId.self.email",
      applicantDetails.emailAddress.map(_.email),
      Some(applicantRoutes.CaptureEmailAddressController.show.url)
    )

    val telephone = optSummaryListRowString(
      if (isTransactor) s"$sectionId.transactor.telephone" else s"$sectionId.self.telephone",
      applicantDetails.telephoneNumber.map(_.telephone),
      Some(applicantRoutes.CaptureTelephoneNumberController.show.url)
    )

    val homeAddress = optSummaryListRowSeq(
      if (isTransactor) s"$sectionId.transactor.homeAddress" else s"$sectionId.self.homeAddress",
      applicantDetails.homeAddress.flatMap(_.address).map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
          Some(applicantRoutes.InternationalHomeAddressController.show.url)
        case _ =>
          Some(applicantRoutes.HomeAddressController.redirectToAlf.url)
      }
    )

    val moreThanThreeYears = optSummaryListRowBoolean(
      if (isTransactor) s"$sectionId.transactor.moreThanThreeYears" else s"$sectionId.self.moreThanThreeYears",
      applicantDetails.previousAddress.map(_.yesNo),
      Some(applicantRoutes.PreviousAddressController.show.url)
    )

    val previousAddress = optSummaryListRowSeq(
      if (isTransactor) s"$sectionId.transactor.previousAddress" else s"$sectionId.self.previousAddress",
      applicantDetails.previousAddress.flatMap(_.address).map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
          Some(applicantRoutes.InternationalPreviousAddressController.show.url)
        case _ =>
          Some(applicantRoutes.PreviousAddressController.show.url)
      }
    )

    val applicantSummaryListRows = Seq(
      fullName,
      dob,
      nino,
      roleInTheBusiness,
      changedName,
      formerName,
      formerNameDate,
      homeAddress,
      moreThanThreeYears,
      previousAddress,
      email,
      telephone
    ).flatten

    applicantSummaryListRows
  }

  def generateLeadPartnerSummaryListRows(vatScheme: VatScheme)(implicit messages: Messages): Seq[SummaryListRow] = {
    val leadPartner: Option[Entity] = for {
      partners <- vatScheme.entities
      leadPartner <- partners.headOption
    } yield leadPartner

    leadPartner.map { partner =>
      val url = partner.partyType match {
        case Individual | NETP => Some(grsRoutes.PartnerSoleTraderIdController.startJourney(leadEntityIndex).url)
        case UkCompany | RegSociety | CharitableOrg => Some(grsRoutes.PartnerIncorpIdController.startJourney(leadEntityIndex).url)
        case ScotPartnership | ScotLtdPartnership | LtdLiabilityPartnership => Some(grsRoutes.PartnerPartnershipIdController.startJourney(leadEntityIndex).url)
        case _ => None
      }
      val uniqueTaxpayerReference = partner.details match {
        case Some(soleTrader: SoleTraderIdEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.uniqueTaxpayerReference",
            optAnswer = soleTrader.sautr,
            optUrl = url
          )
        case Some(partnership: PartnershipIdEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.uniqueTaxpayerReference",
            optAnswer = partnership.sautr,
            optUrl = url
          )
        case Some(incorporated: IncorporatedEntity) =>
          optSummaryListRowString(
            questionId = partner.partyType match {
              case RegSociety => s"$sectionId.leadPartner.uniqueTaxpayerReference"
              case _ => s"$sectionId.leadPartner.companyUniqueTaxpayerReference"
            },
            optAnswer = incorporated.ctutr,
            optUrl = url
          )
        case _ => None
      }
      val companyRegistrationNumber = partner.details match {
        case Some(partnership: PartnershipIdEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.companyNumber",
            optAnswer = partnership.companyNumber,
            optUrl = url
          )
        case Some(incorporated: IncorporatedEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.companyRegistrationNumber",
            optAnswer = Some(incorporated.companyNumber),
            optUrl = url
          )
        case _ => None
      }
      val companyName = partner.details match {
        case Some(partnership: PartnershipIdEntity) if partner.partyType.equals(ScotPartnership) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.partnershipName",
            optAnswer = partnership.companyName,
            optUrl = Some(controllers.business.routes.ScottishPartnershipNameController.show.url)
          )
        case Some(partnership: PartnershipIdEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.partnershipName",
            optAnswer = partnership.companyName,
            optUrl = url
          )
        case Some(incorporated: IncorporatedEntity) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.companyName",
            optAnswer = incorporated.companyName,
            optUrl = url
          )
        case _ => None
      }
      val registeredPostcode = partner.details match {
        case Some(partnership: PartnershipIdEntity) =>
          val questionId = partner.partyType match {
            case ScotLtdPartnership | LtdLiabilityPartnership => s"$sectionId.leadPartner.postcodeForSelfAssessment"
            case _ => s"$sectionId.leadPartner.registeredPostcode"
          }
          optSummaryListRowString(questionId = questionId, optAnswer = partnership.postCode, optUrl = url)
        case _ => None
      }
      val charityHMRCReferenceNumber = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.charityHMRCReferenceNumber",
        optAnswer = partner.details match {
          case Some(incorporated: IncorporatedEntity) => incorporated.chrn
          case _ => None
        },
        optUrl = url)

      Seq(
        leadPartnerEntityType(partner),
        leadPartnerBusinessEntityType(partner),
        uniqueTaxpayerReference,
        companyRegistrationNumber,
        companyName,
        registeredPostcode,
        charityHMRCReferenceNumber
      ).flatten
    }.getOrElse(Nil)
  }

  private def leadPartnerEntityType(leadPartner: Entity)(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRowString(
      questionId = s"$sectionId.leadPartner.partnerType",
      leadPartner.partyType match {
        case Individual | NETP => Some(messages("pages.leadPartnerEntityType.soleTrader"))
        case _ => Some(messages("pages.leadPartnerEntityType.business"))
      },
      Some(controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url)
    )
  }

  private def leadPartnerBusinessEntityType(leadPartner: Entity)(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRowString(
      questionId = s"$sectionId.leadPartner.businessType",
      leadPartner.partyType match {
        case Individual | NETP => None
        case UkCompany => Some(messages("pages.businessLeadPartnerEntityType.ukCompany"))
        case CharitableOrg => Some(messages("pages.businessLeadPartnerEntityType.cio"))
        case LtdLiabilityPartnership => Some(messages("pages.businessLeadPartnerEntityType.limLiaPartner"))
        case RegSociety => Some(messages("pages.businessLeadPartnerEntityType.regSociety"))
        case ScotLtdPartnership => Some(messages("pages.businessLeadPartnerEntityType.scotLimPartner"))
        case ScotPartnership => Some(messages("pages.businessLeadPartnerEntityType.scotPartner"))
      },
      Some(controllers.applicant.routes.BusinessLeadPartnerEntityController.showPartnerEntityType.url)
    )
  }
}
