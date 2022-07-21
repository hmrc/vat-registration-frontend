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

package viewmodels.tasklist

import config.FrontendAppConfig
import featureswitch.core.config.{FeatureSwitching, UseSoleTraderIdentification}
import models.CurrentProfile
import models.api._
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.http.InternalServerException

import javax.inject.{Inject, Singleton}

@Singleton
class AboutYouTaskList @Inject()(verifyBusinessTaskList: VerifyBusinessTaskList,
                                 aboutYouTransactorTaskList: AboutYouTransactorTaskList) extends FeatureSwitching {

  private def isIndividualType(scheme: VatScheme): Boolean =
    Seq(Individual, NETP).exists(scheme.partyType.contains)

  private def isPartnershipWithIndLeadPartner(scheme: VatScheme): Boolean =
    Seq(Partnership, LtdPartnership, ScotPartnership, ScotLtdPartnership).exists(scheme.partyType.contains) &&
      scheme.partners.exists(_.filter(_.isLeadPartner)
        .exists(partner => partner.partyType == Individual || partner.partyType == NETP))

  // scalastyle:off
  def personalDetailsRow(implicit profile: CurrentProfile) = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutYou.personalDetails",
    url = scheme => {
      if (isIndividualType(scheme) || isPartnershipWithIndLeadPartner(scheme)) {
        controllers.applicant.routes.FormerNameController.show.url
      } else {
        if (scheme.partyType.contains(UkCompany) && !isEnabled(UseSoleTraderIdentification)) {
          controllers.applicant.routes.PersonalDetailsValidationController.startPersonalDetailsValidationJourney().url
        } else {
          controllers.applicant.routes.IndividualIdentificationController.startJourney.url
        }
      }
    },
    tagId = "applicantPersonalDetailsRow",
    checks = scheme => {
      Seq(scheme.applicantDetails.exists(_.hasFormerName.isDefined))
      .++ {
        if (isIndividualType(scheme) || isPartnershipWithIndLeadPartner(scheme)) {
          Nil
        } else if (scheme.partyType.contains(LtdLiabilityPartnership)) {
          Seq(scheme.applicantDetails.exists(_.personalDetails.isDefined))
        } else {
          Seq(
            scheme.applicantDetails.exists(_.personalDetails.isDefined),
            scheme.applicantDetails.exists(_.roleInTheBusiness.isDefined)
          )
        }
      }
      .++ {
        if (scheme.applicantDetails.exists(_.hasFormerName.contains(true))) {
          Seq(
            scheme.applicantDetails.exists(_.formerName.isDefined),
            scheme.applicantDetails.exists(_.formerNameDate.isDefined)
          )
        } else {
          Nil
        }
      }
    },
    prerequisites = scheme => Seq(
      if (scheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(aboutYouTransactorTaskList.transactorPersonalDetailsRow) else None,
      if (Seq(Partnership, LtdPartnership, ScotLtdPartnership, ScotPartnership).exists(scheme.partyType.contains)) Some(leadPartnerDetailsRow) else None,
      Some(verifyBusinessTaskList.businessInfoRow)
    ).flatten
  )

  def leadPartnerDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.leadPartnerDetails",
      url = _ => controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url,
      tagId = "leadPartnerDetailsRow",
      checks = scheme => Seq(scheme.partners.exists(_.exists(_.isLeadPartner))),
      prerequisites = _ => Seq(verifyBusinessTaskList.businessInfoRow)
    )
  }

  def addressDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.addressDetails",
      url = vatScheme => resolveAddressRowUrl(vatScheme),
      tagId = "addressDetailsRow",
      checks = addressDetailsChecks,
      prerequisites = _ => Seq(personalDetailsRow)
    )
  }

  def contactDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.contactDetails",
      url = _ => controllers.applicant.routes.CaptureEmailAddressController.show.url,
      tagId = "contactDetailsRow",
      checks = scheme => {
        Seq(
          scheme.applicantDetails.exists(_.emailAddress.isDefined),
          scheme.applicantDetails.exists(_.telephoneNumber.isDefined)
        )
      }.++ {
        if (scheme.eligibilitySubmissionData.exists(_.isTransactor)) {
          Nil
        } else {
          Seq(scheme.applicantDetails.exists(_.emailVerified.exists(_.emailVerified)))
        }
      },
      prerequisites = _ => Seq(addressDetailsRow)
    )
  }

  def buildLeadPartnerRow(vatScheme: VatScheme)(implicit profile: CurrentProfile): Option[TaskListSectionRow] = {
    vatScheme.partyType match {
      case Some(Partnership) | Some(LtdPartnership) | Some(ScotPartnership) | Some(ScotLtdPartnership) =>
        Some(leadPartnerDetailsRow.build(vatScheme))
      case _ =>
        None
    }
  }

  private def resolveAddressRowUrl(vatScheme: VatScheme): String = {
    vatScheme.partyType match {
      case Some(NETP) | Some(NonUkNonEstablished) =>
        controllers.applicant.routes.InternationalHomeAddressController.show.url
      case Some(_) =>
        controllers.applicant.routes.HomeAddressController.redirectToAlf.url
      case None =>
        throw new InternalServerException("[AboutYouTaskList][resolveAddressRowUrl] Failed to initiate address details task list due to missing party type")
    }
  }

  private def addressDetailsChecks(scheme: VatScheme) = {
    Seq(
      Some(scheme.applicantDetails.exists(_.homeAddress.isDefined)),
      if (scheme.applicantDetails.flatMap(_.previousAddress).exists(_.yesNo)) {
        None
      } else {
        Some(scheme.applicantDetails.exists(_.previousAddress.exists(_.address.isDefined)))
      }
    ).flatten
  }

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = if(vatScheme.eligibilitySubmissionData.exists(_.isTransactor)) messages("tasklist.aboutBusinessContact.heading") else messages("tasklist.aboutYou.heading"),
      rows = Seq(
        buildLeadPartnerRow(vatScheme),
        Some(personalDetailsRow.build(vatScheme)),
        Some(addressDetailsRow.build(vatScheme)),
        Some(contactDetailsRow.build(vatScheme))
      ).flatten
    )
}
