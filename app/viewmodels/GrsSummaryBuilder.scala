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
import featureswitch.core.config._
import models.api._
import models.external._
import models.external.soletraderid.OverseasIdentifierDetails
import models.view.SummaryListRowUtils._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}

// scalastyle:off
@Singleton
class GrsSummaryBuilder @Inject()(configConnector: ConfigConnector) extends FeatureSwitching {

  val sectionId: String = "cya.grsDetails"

  def build(vatScheme: VatScheme)(implicit messages: Messages): SummaryList = {

    val partyType: PartyType = vatScheme.eligibilitySubmissionData.map(_.partyType).getOrElse(throw new InternalServerException("[GrsSummaryBuilder] Missing party type"))

    val applicantDetails = vatScheme.applicantDetails.getOrElse(throw new InternalServerException("[GrsSummaryBuilder] Missing GRS details block"))

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
      applicantDetails.entity.flatMap {
        case _: MinorEntity => Some(applicantRoutes.MinorEntityIdController.startJourney.url)
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
        case Partnership | ScotPartnership | LtdLiabilityPartnership | LtdPartnership | ScotLtdPartnership => s"$sectionId.partnershipSaPostcode"
        case _ => ""
      },
      applicantDetails.entity.flatMap {
        case partnership: PartnershipIdEntity => partnership.postCode
        case _ => None
      },
      Some(applicantRoutes.PartnershipIdController.startJourney.url)
    )

    val minorEntityPostcode = optSummaryListRowString(
      partyType match {
        case UnincorpAssoc => s"$sectionId.unincorpAssocPostcode"
        case Trust => s"$sectionId.trustSaPostcode"
        case _ => ""
      },
      applicantDetails.entity.flatMap {
        case minorEntity: MinorEntity => minorEntity.postCode
        case _ => None
      },
      Some(applicantRoutes.MinorEntityIdController.startJourney.url)
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

    SummaryList(Seq(
      companyNumber,
      businessName,
      ctutr,
      sautr,
      partnershipPostcode,
      minorEntityPostcode,
      overseasIdentifier,
      overseasCountry,
      chrn
    ).flatten)
  }
}