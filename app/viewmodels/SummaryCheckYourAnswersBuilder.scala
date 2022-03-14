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

import connectors.ConfigConnector
import controllers.registration.applicant.{routes => applicantRoutes}
import controllers.registration.business.{routes => businessContactRoutes}
import controllers.registration.returns.{routes => returnsRoutes}
import controllers.registration.sicandcompliance.{routes => sicAndCompRoutes}
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import models._
import models.api.returns._
import models.api.{Address, Individual, VatScheme, _}
import models.external._
import models.external.soletraderid.OverseasIdentifierDetails
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import services.FlatRateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.http.InternalServerException

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class SummaryCheckYourAnswersBuilder @Inject()(configConnector: ConfigConnector,
                                               flatRateService: FlatRateService) extends FeatureSwitching {

  val presentationFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")
  val sectionId: String = "cya"

  def generateSummaryList(implicit vatScheme: VatScheme, messages: Messages): SummaryList = {
    implicit val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing party type"))

    SummaryList(
      leadPartnershipSection ++
      applicantDetailsSection ++
      businessContactSection ++
      sicAndComplianceSection ++
      returnsSection ++
      tradingDetailsSection ++
      bankAccountSection ++
      flatRateSchemeSection
    )
  }

  def leadPartnershipSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val leadPartner: Option[PartnerEntity] = for {
      partners <- vatScheme.partners
      leadPartner <- partners.headOption
    } yield leadPartner

    leadPartner.map{ partner =>
      val url = partner.partyType match {
        case Individual | NETP => Some(applicantRoutes.SoleTraderIdentificationController.startPartnerJourney.url)
        case UkCompany | RegSociety | CharitableOrg => Some(applicantRoutes.IncorpIdController.startPartnerJourney.url)
        case ScotPartnership | ScotLtdPartnership | LtdLiabilityPartnership => Some(applicantRoutes.PartnershipIdController.startPartnerJourney.url)
      }
      val firstName = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.firstName",
        optAnswer = partner.details match {
          case soleTrader:SoleTraderIdEntity => Some(soleTrader.firstName)
          case _ => None
        },
        optUrl = url)
      val lastName = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.lastName",
        optAnswer = partner.details match {
          case soleTrader: SoleTraderIdEntity => Some(soleTrader.lastName)
          case _ => None
        },
        optUrl = url)
      val dateOfBirth = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.dateOfBirth",
        optAnswer = partner.details match {
          case soleTrader: SoleTraderIdEntity => Some(soleTrader.dateOfBirth.format(presentationFormatter))
          case _ => None
        },
        optUrl = url)
      val nationalInsuranceNumber = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.nationalInsuranceNumber",
        optAnswer = partner.details match {
          case soleTrader: SoleTraderIdEntity => soleTrader.nino
          case _ => None
        },
        optUrl = url)
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
      /*    TODO: uncomment it once homeAddress is available
      val homeAddress = optSummaryListRowString(
        questionId = s"$sectionId.leadPartner.homeAddress",
        optAnswer = partner.details match {
          case soleTrader: SoleTraderIdEntity => soleTrader.homeAddress
          case _ => None
        },
        optUrl = url)
      */
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
          optSummaryListRowString(questionId = questionId, optAnswer =  partnership.postCode, optUrl = url)
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
        firstName,
        lastName,
        dateOfBirth,
        nationalInsuranceNumber,
        uniqueTaxpayerReference,
        companyRegistrationNumber,
        companyName,
        registeredPostcode,
        charityHMRCReferenceNumber
      ).flatten
    }.getOrElse(Nil)
  }

  def applicantDetailsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing applicant details block"))

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

    val companyNumber = optSummaryListRowString(
      s"$sectionId.companyNumber",
      applicantDetails.entity.flatMap {
        case incorpIdEntity: IncorporatedEntity => Some(incorpIdEntity.companyNumber)
        case partnerEntity: PartnershipIdEntity => partnerEntity.companyNumber
        case _ => None
      },
      applicantDetails.entity.flatMap {
        case _: IncorporatedEntity => Some(applicantRoutes.IncorpIdController.startJourney.url)
        case _: PartnershipIdEntity => Some(applicantRoutes.PartnershipIdController.startJourney.url)
        case _ => None
      }
    )

    val businessName = optSummaryListRowString(
      s"$sectionId.businessName",
      applicantDetails.entity.flatMap {
        case incorpIdEntity: IncorporatedEntity => incorpIdEntity.companyName
        case minorEntity: MinorEntity => minorEntity.companyName
        case partnerEntity: PartnershipIdEntity => partnerEntity.companyName
        case _ => None
      },
      applicantDetails.entity.flatMap {
        case _: IncorporatedEntity => Some(applicantRoutes.IncorpIdController.startJourney.url)
        case _: MinorEntity => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _: PartnershipIdEntity if List(Partnership, ScotPartnership).contains(partyType) =>
          Some(controllers.registration.business.routes.PartnershipNameController.show.url)
        case _: PartnershipIdEntity => Some(applicantRoutes.PartnershipIdController.startJourney.url)
        case _ => None
      }
    )

    val ctutr = optSummaryListRowString(
      s"$sectionId.ctutr",
      applicantDetails.entity.flatMap {
        case incorpIdEntity: IncorporatedEntity => incorpIdEntity.ctutr
        case minorEntity: MinorEntity => minorEntity.ctutr
        case _ => None
      },
      partyType match {
        case NonUkNonEstablished => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _ => Some(applicantRoutes.IncorpIdController.startJourney.url)
      }
    )

    val sautr = optSummaryListRowString(
      s"$sectionId.sautr",
      applicantDetails.entity.flatMap {
        case soleTrader: SoleTraderIdEntity => soleTrader.sautr
        case business: MinorEntity => business.sautr
        case partnership: PartnershipIdEntity => partnership.sautr
        case _ => None
      },
      applicantDetails.entity.flatMap {
        case _: SoleTraderIdEntity => Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
        case _: MinorEntity => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _: PartnershipIdEntity => Some(applicantRoutes.PartnershipIdController.startJourney.url)
        case _ => None
      }
    )

    val partnershipPostcode = optSummaryListRowString(
      partyType match {
        case Partnership | ScotPartnership => s"$sectionId.regPostcode"
        case LtdLiabilityPartnership | LtdPartnership | ScotLtdPartnership => s"$sectionId.saPostcode"
        case _ => ""
      },
      applicantDetails.entity.flatMap {
        case partnership: PartnershipIdEntity => partnership.postCode
        case _ => None
      },
      Some(applicantRoutes.PartnershipIdController.startJourney.url)
    )

    val overseasIdentifier = optSummaryListRowString(
      s"$sectionId.overseasIdentifier",
      applicantDetails.entity.flatMap {
        case soleTraderIdEntity: SoleTraderIdEntity => soleTraderIdEntity.overseas.map(_.taxIdentifier)
        case minorEntity: MinorEntity => minorEntity.overseas.map(_.taxIdentifier)
        case _ => None
      },
      partyType match {
        case NonUkNonEstablished => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _ => Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
      }
    )

    def optCountryName(overseas: Option[OverseasIdentifierDetails]): Option[String] = for {
      countryCode <- overseas.map(_.country)
      country = configConnector.countries.find(_.code.contains(countryCode))
      optCountryName <- country.flatMap(_.name)
    } yield optCountryName

    val overseasCountry = optSummaryListRowString(
      s"$sectionId.overseasCountry",
      applicantDetails.entity.flatMap {
        case soleTraderEntity: SoleTraderIdEntity => optCountryName(soleTraderEntity.overseas)
        case minorEntity: MinorEntity => optCountryName(minorEntity.overseas)
        case _ => None
      },
      partyType match {
        case NonUkNonEstablished => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _ => Some(applicantRoutes.SoleTraderIdentificationController.startJourney.url)
      }
    )

    val chrn = optSummaryListRowString(
      s"$sectionId.chrn",
      applicantDetails.entity.flatMap {
        case incorporatedEntity: IncorporatedEntity => incorporatedEntity.chrn
        case minorEntity: MinorEntity => minorEntity.chrn
        case _ => None
      },
      partyType match {
        case CharitableOrg => Some(applicantRoutes.IncorpIdController.startJourney.url)
        case Trust | UnincorpAssoc | NonUkNonEstablished => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
        case _ => None
      }
    )

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

    val trn = optSummaryListRowString(
      s"$sectionId.trn",
      applicantDetails.personalDetails.flatMap(_.trn),
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

    val roleInTheBusiness = optSummaryListRowString(
      s"$sectionId.roleInTheBusiness",
      applicantDetails.roleInTheBusiness.collect {
        case Director => "pages.roleInTheBusiness.radio1"
        case CompanySecretary => "pages.roleInTheBusiness.radio2"
      },
      Some(applicantRoutes.CaptureRoleInTheBusinessController.show.url)
    )

    val formerName = optSummaryListRowBoolean(
      s"$sectionId.formerName",
      applicantDetails.hasFormerName,
      Some(applicantRoutes.FormerNameController.show.url)
    )

    val formerNameCapture = optSummaryListRowString(
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
        case NETP =>
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
        case NETP =>
          Some(applicantRoutes.InternationalPreviousAddressController.show.url)
        case _ =>
          Some(applicantRoutes.PreviousAddressController.show.url)
      }
    )

    Seq(
      companyNumber,
      businessName,
      ctutr,
      sautr,
      partnershipPostcode,
      overseasIdentifier,
      overseasCountry,
      chrn,
      firstName,
      lastName,
      nino,
      trn,
      dob,
      roleInTheBusiness,
      formerName,
      formerNameCapture,
      formerNameDate,
      email,
      telephone,
      homeAddress,
      moreThanThreeYears,
      previousAddress
    ).flatten
  }

  def businessContactSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val businessContact: BusinessContact = vatScheme.businessContact.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing business contact block"))

    val businessEmailRow = optSummaryListRowString(
      s"$sectionId.emailBusiness",
      businessContact.companyContactDetails.map(_.email),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

    val businessDaytimePhoneNumberRow = optSummaryListRowString(
      s"$sectionId.daytimePhoneBusiness",
      businessContact.companyContactDetails.flatMap(_.phoneNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

    val businessMobilePhoneNumberRow = optSummaryListRowString(
      s"$sectionId.mobileBusiness",
      businessContact.companyContactDetails.flatMap(_.mobileNumber),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

    val businessWebsiteRow = optSummaryListRowString(
      s"$sectionId.website",
      businessContact.companyContactDetails.flatMap(_.websiteAddress),
      Some(businessContactRoutes.BusinessContactDetailsController.show.url)
    )

    val ppobRow = optSummaryListRowSeq(
      s"$sectionId.ppob",
      businessContact.ppobAddress.map(Address.normalisedSeq),
      partyType match {
        case NETP =>
          Some(businessContactRoutes.InternationalPpobAddressController.show.url)
        case _ =>
          Some(businessContactRoutes.PpobAddressController.startJourney.url)
      }
    )

    val contactPreferenceRow = optSummaryListRowString(
      s"$sectionId.contactPreference",
      businessContact.contactPreference.map(_.toString),
      Some(controllers.routes.ContactPreferenceController.showContactPreference.url)
    )

    Seq(
      businessEmailRow,
      businessDaytimePhoneNumberRow,
      businessMobilePhoneNumberRow,
      businessWebsiteRow,
      ppobRow,
      contactPreferenceRow
    ).flatten
  }

  def sicAndComplianceSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val sicAndCompliance: SicAndCompliance = vatScheme.sicAndCompliance.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing sic and compliance block"))

    val landAndPropertyRow = optSummaryListRowBoolean(
      s"$sectionId.landAndProperty",
      sicAndCompliance.hasLandAndProperty,
      Some(sicAndCompRoutes.LandAndPropertyController.show.url)
    )

    val companyBusinessDescriptionRow = optSummaryListRowString(
      s"$sectionId.businessDescription",
      sicAndCompliance.description.map(_.description),
      Some(sicAndCompRoutes.BusinessActivityDescriptionController.show.url)
    )

    val mainActivityRow = optSummaryListRowString(
      s"$sectionId.mainSicCode",
      sicAndCompliance.mainBusinessActivity.flatMap(_.mainBusinessActivity.map(_.description)),
      Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)
    )

    val sicCodesRow = optSummaryListRowSeq(
      s"$sectionId.sicCodes",
      sicAndCompliance.businessActivities.map(_.sicCodes.map(_.description)),
      Some(controllers.routes.SicAndComplianceController.showMainBusinessActivity.url)
    )

    val confirmIndustryClassificationCodesRow = optSummaryListRowSeq(
      s"$sectionId.businessActivities",
      sicAndCompliance.businessActivities.map(codes => codes.sicCodes.map(
        sicCode => sicCode.code + " - " + sicCode.description
      )),
      Some(controllers.routes.SicAndComplianceController.returnToICL.url)
    )

    val providingWorkersRow = optSummaryListRowBoolean(
      s"$sectionId.supplyWorkers",
      sicAndCompliance.supplyWorkers.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersController.show.url)
    )

    val numberOfWorkersRow = optSummaryListRowString(
      s"$sectionId.numberOfWorkers",
      sicAndCompliance.workers.map(_.numberOfWorkers.toString),
      Some(sicAndCompRoutes.WorkersController.show.url)
    )

    val intermediarySupplyRow = optSummaryListRowBoolean(
      s"$sectionId.intermediarySupply",
      sicAndCompliance.intermediarySupply.map(_.yesNo),
      Some(sicAndCompRoutes.SupplyWorkersIntermediaryController.show.url)
    )

    Seq(
      landAndPropertyRow,
      companyBusinessDescriptionRow,
      mainActivityRow,
      sicCodesRow,
      confirmIndustryClassificationCodesRow,
      providingWorkersRow,
      numberOfWorkersRow,
      intermediarySupplyRow
    ).flatten
  }

  def returnsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val returns: Returns = vatScheme.returns.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing returns block"))
    val mandatoryRegistration: Boolean = vatScheme.eligibilitySubmissionData.map(_.threshold).exists(_.mandatoryRegistration)

    val startDateRow = optSummaryListRowString(
      s"$sectionId.startDate",
      returns.startDate match {
        case Some(date) => Some(date.format(presentationFormatter))
        case None if partyType.equals(NETP) => None
        case None => Some(s"$sectionId.mandatoryStartDate")
      },
      Some(controllers.registration.returns.routes.VatRegStartDateResolverController.resolve.url)
    )

    val zeroRatedRow = optSummaryListRowString(
      s"$sectionId.zeroRated",
      returns.zeroRatedSupplies.map(Formatters.currency),
      Some(returnsRoutes.ZeroRatedSuppliesController.show.url)
    )

    val expectClaimRefundsRow = optSummaryListRowBoolean(
      s"$sectionId.claimRefunds",
      returns.reclaimVatOnMostReturns,
      Some(returnsRoutes.ClaimRefundsController.show.url)
    )

    val accountingPeriodRow = optSummaryListRowString(
      s"$sectionId.accountingPeriod",
      (returns.returnsFrequency, returns.staggerStart) match {
        case (Some(Monthly), _) =>
          Some(s"$sectionId.accountingPeriod.monthly")
        case (Some(Quarterly), Some(period)) =>
          Some(s"$sectionId.accountingPeriod.${period.toString.substring(0, 3).toLowerCase()}")
        case (Some(Annual), _) =>
          Some(s"$sectionId.accountingPeriod.annual")
        case _ => throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Invalid accounting period")
      },
      Some(returnsRoutes.ReturnsController.accountPeriodsPage.url)
    )

    val lastMonthOfAccountingYearRow = optSummaryListRowString(
      s"$sectionId.lastMonthOfAccountingYear",
      returns.staggerStart match {
        case Some(period: AnnualStagger) => Some(s"$sectionId.lastMonthOfAccountingYear.${period.toString}")
        case _ => None
      },
      Some(returnsRoutes.LastMonthOfAccountingYearController.show.url)
    )

    val paymentFrequencyRow = optSummaryListRowString(
      s"$sectionId.paymentFrequency",
      returns.annualAccountingDetails.flatMap(_.paymentFrequency).map { paymentFrequency =>
        s"$sectionId.paymentFrequency.${paymentFrequency.toString}"
      },
      Some(returnsRoutes.PaymentFrequencyController.show.url)
    )

    val paymentMethodRow = optSummaryListRowString(
      s"$sectionId.paymentMethod",
      returns.annualAccountingDetails.flatMap(_.paymentMethod).map { paymentMethod =>
        s"$sectionId.paymentMethod.${paymentMethod.toString}"
      },
      Some(returnsRoutes.PaymentMethodController.show.url)
    )

    val sendGoodsOverseas = optSummaryListRowBoolean(
      s"$sectionId.sendGoodsOverseas",
      returns.overseasCompliance.flatMap(_.goodsToOverseas),
      Some(returnsRoutes.SendGoodsOverseasController.show.url)
    )

    val sendGoodsToEu = optSummaryListRowBoolean(
      s"$sectionId.sendGoodsToEu",
      returns.overseasCompliance.flatMap(_.goodsToEu),
      Some(returnsRoutes.SendEUGoodsController.show.url)
    )

    val storingGoods = optSummaryListRowString(
      s"$sectionId.storingGoods",
      returns.overseasCompliance.flatMap(_.storingGoodsForDispatch).map {
        case StoringWithinUk => s"$sectionId.storingGoods.uk"
        case StoringOverseas => s"$sectionId.storingGoods.overseas"
      },
      Some(returnsRoutes.StoringGoodsController.show.url)
    )

    val dispatchFromWarehouse = optSummaryListRowBoolean(
      s"$sectionId.dispatchFromWarehouse",
      returns.overseasCompliance.flatMap(_.usingWarehouse),
      Some(returnsRoutes.DispatchFromWarehouseController.show.url)
    )

    val warehouseNumber = optSummaryListRowString(
      s"$sectionId.warehouseNumber",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseNumber),
      Some(returnsRoutes.WarehouseNumberController.show.url)
    )

    val warehouseName = optSummaryListRowString(
      s"$sectionId.warehouseName",
      returns.overseasCompliance.flatMap(_.fulfilmentWarehouseName),
      Some(returnsRoutes.WarehouseNameController.show.url)
    )

    val sellOrMoveNip = optSummaryListRowBoolean(
      s"$sectionId.sellOrMoveNip",
      returns.northernIrelandProtocol.flatMap(_.goodsToEU).map(_.answer),
      Some(returnsRoutes.SellOrMoveNipController.show.url)
    )

    val receiveGoodsNip = optSummaryListRowBoolean(
      s"$sectionId.receiveGoodsNip",
      returns.northernIrelandProtocol.flatMap(_.goodsFromEU).map(_.answer),
      Some(returnsRoutes.ReceiveGoodsNipController.show.url)
    )

    Seq(
      startDateRow,
      if (vatScheme.eligibilitySubmissionData.map(_.estimates.turnoverEstimate).contains(0)) None else zeroRatedRow,
      expectClaimRefundsRow,
      accountingPeriodRow,
      lastMonthOfAccountingYearRow,
      paymentFrequencyRow,
      paymentMethodRow,
      sendGoodsOverseas,
      sendGoodsToEu,
      storingGoods,
      dispatchFromWarehouse,
      warehouseNumber,
      warehouseName,
      sellOrMoveNip,
      receiveGoodsNip
    ).flatten
  }

  def tradingDetailsSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val tradingDetails: TradingDetails = vatScheme.tradingDetails.getOrElse(throw new InternalServerException("[SummaryCheckYourAnswersBuilder] Missing trading details block"))
    val tradingNameOptional: Boolean = Seq(UkCompany, RegSociety, CharitableOrg, NonUkNonEstablished, Trust, UnincorpAssoc).contains(partyType)

    val shortOrgNameRow = optSummaryListRowString(
      s"$sectionId.shortOrgName",
      tradingDetails.shortOrgName,
      Some(controllers.registration.business.routes.ShortOrgNameController.show.url)
    )

    val tradingNameRow = optSummaryListRowString(
      if (tradingNameOptional) {
        s"$sectionId.tradingName"
      } else {
        s"$sectionId.mandatoryName"
      },
      tradingDetails.tradingNameView.flatMap(_.tradingName) match {
        case None => Some("app.common.no")
        case optTradingName => optTradingName
      },
      if (tradingNameOptional) {
        Some(controllers.registration.business.routes.TradingNameController.show.url)
      } else {
        Some(controllers.registration.business.routes.MandatoryTradingNameController.show.url)
      }
    )

    val applyForEoriRow = optSummaryListRowBoolean(
      s"$sectionId.applyForEori",
      tradingDetails.euGoods,
      Some(controllers.registration.business.routes.ApplyForEoriController.show.url)
    )

    val importsOrExportsRow = optSummaryListRowBoolean(
      s"$sectionId.importsOrExports",
      tradingDetails.tradeVatGoodsOutsideUk,
      Some(controllers.registration.business.routes.ImportsOrExportsController.show.url)
    )

    Seq(
      shortOrgNameRow,
      tradingNameRow,
      importsOrExportsRow,
      applyForEoriRow
    ).flatten
  }

  def bankAccountSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val bankAccount: Option[BankAccount] = vatScheme.bankAccount

    val accountIsProvidedRow = optSummaryListRowBoolean(
      s"$sectionId.companyBankAccount",
      bankAccount.map(_.isProvided),
      Some(controllers.registration.bankdetails.routes.HasBankAccountController.show.url)
    )

    val companyBankAccountDetails = optSummaryListRowSeq(
      s"$sectionId.companyBankAccount.details",
      partyType match {
        case NETP => bankAccount.flatMap(_.overseasDetails.map(OverseasBankDetails.overseasBankSeq))
        case _ => bankAccount.flatMap(_.details.map(BankAccountDetails.bankSeq))
      },
      partyType match {
        case NETP => Some(controllers.registration.bankdetails.routes.OverseasBankAccountController.show.url)
        case _ => Some(controllers.registration.bankdetails.routes.UkBankAccountDetailsController.show.url)
      }
    )

    val noUKBankAccount = optSummaryListRowString(
      s"$sectionId.companyBankAccount.reason",
      bankAccount.flatMap(_.reason).map {
        case BeingSetup => "pages.noUKBankAccount.reason.beingSetup"
        case OverseasAccount => "pages.noUKBankAccount.reason.overseasAccount"
        case NameChange => "pages.noUKBankAccount.reason.nameChange"
      },
      Some(controllers.registration.bankdetails.routes.NoUKBankAccountController.show.url)
    )

    Seq(
      accountIsProvidedRow,
      companyBankAccountDetails,
      noUKBankAccount
    ).flatten
  }

  def flatRateSchemeSection(implicit vatScheme: VatScheme, partyType: PartyType, messages: Messages): Seq[SummaryListRow] = {

    val optFlatRateScheme: Option[FlatRateScheme] = vatScheme.flatRateScheme
    val isLimitedCostTrader: Boolean = optFlatRateScheme.exists(_.limitedCostTrader.contains(true))

    val joinFrsRow = optSummaryListRowBoolean(
      s"$sectionId.joinFrs",
      optFlatRateScheme.flatMap(_.joinFrs),
      Some(controllers.registration.flatratescheme.routes.JoinFlatRateSchemeController.show.url)
    )

    val costsInclusiveRow = optSummaryListRowBoolean(
      s"$sectionId.costsInclusive",
      optFlatRateScheme.flatMap(_.overBusinessGoods),
      Some(controllers.routes.FlatRateController.annualCostsInclusivePage.url)
    )

    val estimateTotalSalesRow = optSummaryListRowString(
      s"$sectionId.estimateTotalSales",
      optFlatRateScheme.flatMap(_.estimateTotalSales.map("%,d".format(_))).map(sales => s"£$sales"),
      Some(controllers.registration.flatratescheme.routes.EstimateTotalSalesController.estimateTotalSales.url)
    )

    val costsLimitedRow = optSummaryListRowBoolean(
      s"$sectionId.costsLimited",
      optFlatRateScheme.flatMap(_.overBusinessGoodsPercent),
      Some(controllers.routes.FlatRateController.annualCostsLimitedPage.url),
      Seq(optFlatRateScheme.flatMap(_.estimateTotalSales.map(v => flatRateService.applyPercentRoundUp(v))).map("%,d".format(_)).getOrElse("0"))
    )

    val flatRatePercentageRow = optSummaryListRowBoolean(
      s"$sectionId.flatRate",
      optFlatRateScheme.flatMap(_.useThisRate),
      Some(
        if (isLimitedCostTrader) controllers.routes.FlatRateController.registerForFrsPage.url
        else controllers.routes.FlatRateController.yourFlatRatePage.url
      ),
      Seq(
        if (isLimitedCostTrader) FlatRateService.defaultFlatRate.toString
        else optFlatRateScheme.flatMap(_.percent).getOrElse(0.0).toString
      )
    )

    val businessSectorRow = optSummaryListRowString(
      s"$sectionId.businessSector",
      optFlatRateScheme.flatMap(_.categoryOfBusiness.filter(_.nonEmpty).map(frsId => configConnector.getBusinessTypeDetails(frsId)._1)),
      Some(controllers.registration.flatratescheme.routes.ChooseBusinessTypeController.show.url)
    )

    Seq(
      joinFrsRow,
      costsInclusiveRow,
      estimateTotalSalesRow,
      costsLimitedRow,
      flatRatePercentageRow,
      businessSectorRow
    ).flatten
  }
}
