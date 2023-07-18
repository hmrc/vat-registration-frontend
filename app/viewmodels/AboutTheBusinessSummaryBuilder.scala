/*
 * Copyright 2023 HM Revenue & Customs
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
import featuretoggle.FeatureToggleSupport
import models.api._
import models.external.{BusinessEntity, MinorEntity, PartnershipIdEntity}
import models.view.SummaryListRowUtils._
import models.{ApplicantDetails, Business, English, Entity, Welsh}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import javax.inject.Inject

// scalastyle:off
class AboutTheBusinessSummaryBuilder @Inject()(govukSummaryList: GovukSummaryList) extends FeatureToggleSupport {

  val sectionId = "cya.aboutTheBusiness"

  private def missingSection(section: String) =
    new InternalServerException(s"[AboutTheBusinessCheckYourAnswersBuilder] Couldn't construct CYA due to missing section: $section")

  def build(vatScheme: VatScheme)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): HtmlFormat.Appendable = {
    val businessEntity = vatScheme.applicantDetails.flatMap(_.entity)
    val business = vatScheme.business.getOrElse(throw missingSection("Business details"))
    val eligibilityData = vatScheme.eligibilitySubmissionData.getOrElse(throw missingSection("Eligibility"))
    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw missingSection("GRS"))
    val entities = vatScheme.entities

    HtmlFormat.fill(List(
      govukSummaryList(SummaryList(
        rows = List(
          partnershipMembers(entities),
          shortOrgName(business),
          businessName(applicantDetails),
          partnershipName(eligibilityData.partyType, applicantDetails),
          tradingName(business, eligibilityData.partyType, businessEntity),
          ppobAddress(business, eligibilityData.partyType, eligibilityData.fixedEstablishmentInManOrUk),
          businessEmailAddress(business),
          businessDaytimePhoneNumber(business),
          businessHasWebsite(business),
          businessWebsite(business),
          correspondenceLanguage(business),
          contactPreference(business),
          buySellLandOrProperty(business),
          businessDescription(business),
          otherBusinessActivities(business),
          mainBusinessActivity(business),
          supplyWorkers(business)
        ).flatten ++
          complianceSection(business)
      ))
    ))
  }

  private def partnershipMembers(entities: Option[List[Entity]])(implicit messages: Messages): Option[SummaryListRow] = {
    if (entities.exists(_.length > 1)) {
      optSummaryListRowSeq(
        questionId = s"$sectionId.partnershipMembers",
        optAnswers = {
          entities.map(_.collect {
            case entity if entity.isLeadPartner.contains(false) && entity.isModelComplete(false) =>
              entity.displayName
          }.flatten)
        },
        Some(controllers.partners.routes.PartnerSummaryController.show.url)
      )
    } else {
      None
    }
  }

  private def ppobAddress(business: Business, partyType: PartyType, fixedEstablishment: Boolean)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowSeq(
      s"$sectionId.homeAddress",
      business.ppobAddress.map(Address.normalisedSeq),
      partyType match {
        case NETP | NonUkNonEstablished if !fixedEstablishment =>
          Some(controllers.business.routes.InternationalPpobAddressController.show.url)
        case _ =>
          Some(controllers.business.routes.PpobAddressController.startJourney.url)
      }
    )

  private def businessEmailAddress(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.emailBusiness",
      business.email,
      Some(controllers.business.routes.BusinessEmailController.show.url)
    )

  private def businessDaytimePhoneNumber(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.daytimePhoneBusiness",
      business.telephoneNumber,
      Some(controllers.business.routes.BusinessTelephoneNumberController.show.url)
    )

  private def businessHasWebsite(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.hasWebsite",
      business.hasWebsite,
      Some(controllers.business.routes.HasWebsiteController.show.url)
    )

  private def businessWebsite(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.website",
      business.website,
      Some(controllers.business.routes.BusinessWebsiteAddressController.show.url)
    )

  private def correspondenceLanguage(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.correspondenceLanguage",
      business.welshLanguage match {
        case Some(true) => Some(Welsh.toString)
        case Some(false) => Some(English.toString)
        case None => None
      },
      Some(controllers.business.routes.VatCorrespondenceController.show.url)
    )

  private def contactPreference(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.contactPreference",
      business.contactPreference.map(_.toString),
      Some(controllers.business.routes.ContactPreferenceController.showContactPreference.url)
    )

  private def buySellLandOrProperty(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.buySellLandAndProperty",
      business.hasLandAndProperty,
      Some(controllers.business.routes.LandAndPropertyController.show.url)
    )

  private def businessDescription(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.businessDescription",
      business.businessDescription,
      Some(controllers.business.routes.BusinessActivityDescriptionController.show.url)
    )

  private def mainBusinessActivity(business: Business)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.mainSicCode",
      business.mainBusinessActivity.map(_.getDescription),
      Some(controllers.sicandcompliance.routes.MainBusinessActivityController.show.url)
    )

  private def otherBusinessActivities(business: Business)(implicit messages: Messages, frontendAppConfig: FrontendAppConfig): Option[SummaryListRow] =
    if (business.businessActivities.exists(_.nonEmpty == true)) {
      optSummaryListRowSeq(
        s"$sectionId.sicCodes",
        business.businessActivities.map(codes => codes.map(
          sicCode => sicCode.code + " - " + sicCode.getDescription
        )),
        Some(controllers.sicandcompliance.routes.SicController.startICLJourney.url)
      )
    } else {
      None
    }

  private def supplyWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.supplyWorkers",
      business.labourCompliance.flatMap(_.supplyWorkers),
      Some(controllers.business.routes.SupplyWorkersController.show.url)
    )

  private def numberOfWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.numberOfWorkers",
      business.labourCompliance.flatMap(_.numOfWorkersSupplied.map(_.toString)),
      Some(controllers.business.routes.WorkersController.show.url)
    )

  private def intermediaryArrangingSupplyOfWorkers(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowBoolean(
      s"$sectionId.intermediarySupply",
      business.labourCompliance.flatMap(_.intermediaryArrangement),
      Some(controllers.business.routes.SupplyWorkersIntermediaryController.show.url)
    )

  private def shortOrgName(business: Business)(implicit messages: Messages): Option[SummaryListRow] =
    optSummaryListRowString(
      s"$sectionId.shortOrgName",
      business.shortOrgName,
      Some(controllers.business.routes.ShortOrgNameController.show.url)
    )

  private def businessName(applicantDetails: ApplicantDetails)(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRowString(
      s"$sectionId.businessName",
      applicantDetails.entity.flatMap {
        case minorEntity: MinorEntity => minorEntity.companyName
        case _ => None
      },
      applicantDetails.entity.flatMap {
        case _: MinorEntity => Some(controllers.grs.routes.MinorEntityIdController.startJourney.url)
        case _ => None
      }
    )
  }

  private def partnershipName(partyType: PartyType, applicantDetails: ApplicantDetails)(implicit messages: Messages): Option[SummaryListRow] = {
    optSummaryListRowString(
      s"$sectionId.partnershipName",
      applicantDetails.entity.flatMap {
        case partnerEntity: PartnershipIdEntity if List(Partnership, ScotPartnership).contains(partyType) => partnerEntity.companyName
        case _ => None
      },
      applicantDetails.entity.flatMap {
        case _: PartnershipIdEntity if List(Partnership, ScotPartnership).contains(partyType) =>
          Some(controllers.business.routes.PartnershipNameController.show.url)
        case _ => None
      }
    )
  }

  private def tradingName(business: Business, partyType: PartyType, businessEntity: Option[BusinessEntity])(implicit messages: Messages): Option[SummaryListRow] = {
    val tradingNameOptional = Business.tradingNameOptional(partyType)

    optSummaryListRowString(
      s"$sectionId.tradingName",
      business.tradingName match {
        case None if business.hasTradingName.contains(true) => businessEntity.flatMap(_.getBusinessName)
        case optTradingName => optTradingName
      },
      if (tradingNameOptional) {
        Some(controllers.business.routes.ConfirmTradingNameController.show.url)
      } else {
        Some(controllers.business.routes.CaptureTradingNameController.show.url)
      }
    )
  }

  private def complianceSection(business: Business)(implicit messages: Messages): List[SummaryListRow] =
    if (business.labourCompliance.exists(_.supplyWorkers.contains(true))) {
      List(
        numberOfWorkers(business),
        intermediaryArrangingSupplyOfWorkers(business)
      ).flatten
    } else {
      Nil
    }
}
