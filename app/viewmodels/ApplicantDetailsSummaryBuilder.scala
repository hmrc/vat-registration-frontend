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

import controllers.registration.applicant.{routes => applicantRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import models._
import models.api._
import models.external.{IncorporatedEntity, PartnershipIdEntity, SoleTraderIdEntity}
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class ApplicantDetailsSummaryBuilder @Inject()() extends FeatureSwitching {
  val presentationFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val sectionId: String = "cya.applicantDetails"

  def build(vatScheme: VatScheme)(implicit messages: Messages): SummaryList = {
    val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType)
      .getOrElse(throw new InternalServerException("[ApplicantDetailsSummaryBuilder] Missing party type"))

    SummaryList(generateLeadPartnerSummaryListRows(vatScheme) ++ generateApplicantDetailsSummaryListRows(vatScheme, partyType))
  }

  def generateApplicantDetailsSummaryListRows(vatScheme: VatScheme, partyType: PartyType)(implicit messages: Messages): Seq[SummaryListRow] = {
    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw new InternalServerException("[SummaryApplicantDetailsBuilder] Missing applicant details block"))

    val changePersonalDetailsUrl: String = {
      partyType match {
        case Individual | NETP =>
          applicantRoutes.SoleTraderIdentificationController.startJourney.url
        case Partnership | ScotPartnership | LtdPartnership | ScotLtdPartnership =>
          vatScheme.partners.flatMap(_.headOption.map(_.partyType)) match {
            case Some(Individual | NETP) => applicantRoutes.SoleTraderIdentificationController.startPartnerJourney.url
            case _ => applicantRoutes.IndividualIdentificationController.startJourney.url
          }
        case Trust | UnincorpAssoc | NonUkNonEstablished | LtdLiabilityPartnership =>
          applicantRoutes.IndividualIdentificationController.startJourney.url
        case _ if isEnabled(UseSoleTraderIdentification) => //The low volume entities are already set up to use individual flow, incorp id entities are still switched to PDV in prod
          applicantRoutes.IndividualIdentificationController.startJourney.url
        case _ =>
          applicantRoutes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney.url
      }
    }

    val firstName = optSummaryListRowString(
      s"$sectionId.firstName",
      applicantDetails.personalDetails.map(_.firstName),
      Some(changePersonalDetailsUrl)
    )

    val lastName = optSummaryListRowString(
      s"$sectionId.lastName",
      applicantDetails.personalDetails.map(_.lastName),
      Some(changePersonalDetailsUrl)
    )

    val nino = optSummaryListRowString(
      s"$sectionId.nino",
      applicantDetails.personalDetails.flatMap(_.nino),
      Some(changePersonalDetailsUrl)
    )

    val dob = optSummaryListRowString(
      s"$sectionId.dob",
      for {
        personalDetails <- applicantDetails.personalDetails
        dob <- personalDetails.dateOfBirth
      } yield dob.format(presentationFormatter),
      Some(changePersonalDetailsUrl)
    )

    val roleInTheBusiness = partyType match {
      case NETP | Individual =>
        None
      case _ =>
        optSummaryListRowString(
          s"$sectionId.roleInTheBusiness",
          applicantDetails.roleInTheBusiness.collect {
            case Director => "pages.roleInTheBusiness.radio1"
            case CompanySecretary => "pages.roleInTheBusiness.radio2"
          },
          Some(applicantRoutes.CaptureRoleInTheBusinessController.show.url)
        )
    }

    val changedName = optSummaryListRowBoolean(
      s"$sectionId.formerName",
      applicantDetails.hasFormerName,
      Some(applicantRoutes.FormerNameController.show.url)
    )

    val formerName = optSummaryListRowString(
      s"$sectionId.formerNameCapture",
      applicantDetails.formerName.map(_.asLabel),
      Some(applicantRoutes.FormerNameCaptureController.show.url)
    )

    val formerNameDate = optSummaryListRowString(
      s"$sectionId.formerNameDate",
      applicantDetails.formerNameDate.map(_.date.format(presentationFormatter)),
      Some(applicantRoutes.FormerNameDateController.show.url)
    )

    val email = optSummaryListRowString(
      s"$sectionId.email",
      applicantDetails.emailAddress.map(_.email),
      Some(applicantRoutes.CaptureEmailAddressController.show.url)
    )

    val telephone = optSummaryListRowString(
      s"$sectionId.telephone",
      applicantDetails.telephoneNumber.map(_.telephone),
      Some(applicantRoutes.CaptureTelephoneNumberController.show.url)
    )

    val homeAddress = optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      applicantDetails.homeAddress.flatMap(_.address).map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
          Some(applicantRoutes.InternationalHomeAddressController.show.url)
        case _ =>
          Some(applicantRoutes.HomeAddressController.redirectToAlf.url)
      }
    )

    val moreThanThreeYears = optSummaryListRowBoolean(
      s"$sectionId.moreThanThreeYears",
      applicantDetails.previousAddress.map(_.yesNo),
      Some(applicantRoutes.PreviousAddressController.show.url)
    )

    val previousAddress = optSummaryListRowSeq(
      s"$sectionId.previousAddress",
      applicantDetails.previousAddress.flatMap(_.address).map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished =>
          Some(applicantRoutes.InternationalPreviousAddressController.show.url)
        case _ =>
          Some(applicantRoutes.PreviousAddressController.show.url)
      }
    )

    val applicantSummaryListRows = Seq(
      firstName,
      lastName,
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
    val leadPartner: Option[PartnerEntity] = for {
      partners <- vatScheme.partners
      leadPartner <- partners.headOption
    } yield leadPartner

    leadPartner.map { partner =>
      val url = partner.partyType match {
        case Individual | NETP => Some(applicantRoutes.SoleTraderIdentificationController.startPartnerJourney.url)
        case UkCompany | RegSociety | CharitableOrg => Some(applicantRoutes.IncorpIdController.startPartnerJourney.url)
        case ScotPartnership | ScotLtdPartnership | LtdLiabilityPartnership => Some(applicantRoutes.PartnershipIdController.startPartnerJourney.url)
      }
      val uniqueTaxpayerReference = partner.details match {
        case soleTrader: SoleTraderIdEntity => optSummaryListRowString(
          questionId = s"$sectionId.leadPartner.uniqueTaxpayerReference",
          optAnswer = soleTrader.sautr,
          optUrl = url)
        case partnership: PartnershipIdEntity => optSummaryListRowString(
          questionId = s"$sectionId.leadPartner.uniqueTaxpayerReference",
          optAnswer = partnership.sautr,
          optUrl = url)
        case incorporated: IncorporatedEntity => optSummaryListRowString(
          questionId = partner.partyType match {
            case RegSociety => s"$sectionId.leadPartner.uniqueTaxpayerReference"
            case _ => s"$sectionId.leadPartner.companyUniqueTaxpayerReference"
          },
          optAnswer = incorporated.ctutr,
          optUrl = url)
        case _ => None
      }
      val companyRegistrationNumber = partner.details match {
        case partnership: PartnershipIdEntity => optSummaryListRowString(
          questionId = s"$sectionId.leadPartner.companyNumber",
          optAnswer = partnership.companyNumber,
          optUrl = url)
        case incorporated: IncorporatedEntity => optSummaryListRowString(
          questionId = s"$sectionId.leadPartner.companyRegistrationNumber",
          optAnswer = Some(incorporated.companyNumber),
          optUrl = url)
        case _ => None
      }
      val companyName = partner.details match {
        case partnership: PartnershipIdEntity
          if partner.partyType.equals(ScotLtdPartnership) || partner.partyType.equals(LtdLiabilityPartnership) =>
          optSummaryListRowString(
            questionId = s"$sectionId.leadPartner.partnershipName",
            optAnswer = partnership.companyName,
            optUrl = url)
        case incorporated: IncorporatedEntity => optSummaryListRowString(
          questionId = s"$sectionId.leadPartner.companyName",
          optAnswer = incorporated.companyName,
          optUrl = url)
        case _ => None
      }
      val registeredPostcode = partner.details match {
        case partnership: PartnershipIdEntity =>
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
          case incorporated: IncorporatedEntity => incorporated.chrn
          case _ => None
        },
        optUrl = url)

      Seq(
        uniqueTaxpayerReference,
        companyRegistrationNumber,
        companyName,
        registeredPostcode,
        charityHMRCReferenceNumber
      ).flatten
    }.getOrElse(Nil)
  }

}
