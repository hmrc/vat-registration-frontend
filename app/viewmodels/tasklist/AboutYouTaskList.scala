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

package viewmodels.tasklist


import config.FrontendAppConfig
import models.CurrentProfile
import models.api._

import javax.inject.Singleton

@Singleton
object AboutYouTaskList {

  private def buildMessageKey(suffix: String, vatScheme: VatScheme) = {
    if (vatScheme.eligibilitySubmissionData.exists(_.isTransactor)) s"tasklist.aboutBusinessContact.$suffix" else s"tasklist.aboutYou.$suffix"
  }

  private def isIndividualType(scheme: VatScheme): Boolean =
    Seq(Individual, NETP).exists(scheme.partyType.contains)

  private def isPartnershipWithIndLeadPartner(scheme: VatScheme): Boolean =
    Seq(Partnership, LtdPartnership, ScotPartnership, ScotLtdPartnership).exists(scheme.partyType.contains) &&
      scheme.entities.exists(_.filter(_.isLeadPartner.contains(true))
        .exists(partner => partner.partyType == Individual || partner.partyType == NETP))

  // scalastyle:off
  def personalDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = scheme => buildMessageKey("personalDetails", scheme),
    url = scheme => _ => {
      if (isIndividualType(scheme) || isPartnershipWithIndLeadPartner(scheme)) {
        controllers.applicant.routes.FormerNameController.show.url
      } else {
        controllers.grs.routes.IndividualIdController.startJourney.url
      }
    },
    tagId = "applicantPersonalDetailsRow",
    checks = scheme => {
      Seq(scheme.applicantDetails.exists(_.changeOfName.hasFormerName.isDefined))
        .++ {
          if (isIndividualType(scheme) || isPartnershipWithIndLeadPartner(scheme)) {
            Nil
          } else if (Seq(Partnership, LtdPartnership, ScotLtdPartnership, ScotPartnership, LtdLiabilityPartnership).exists(scheme.partyType.contains)) {
            Seq(scheme.applicantDetails.exists(_.personalDetails.isDefined))
          } else {
            Seq(
              scheme.applicantDetails.exists(_.personalDetails.isDefined),
              scheme.applicantDetails.exists(_.roleInTheBusiness.isDefined)
            )
          }
        }
        .++ {
          if (scheme.applicantDetails.exists(_.changeOfName.hasFormerName.contains(true))) {
            Seq(
              scheme.applicantDetails.exists(_.changeOfName.name.isDefined),
              scheme.applicantDetails.exists(_.changeOfName.change.isDefined)
            )
          } else {
            Nil
          }
        }
    },
    prerequisites = scheme => Seq(
      if (scheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(AboutYouTransactorTaskList.transactorPersonalDetailsRow) else None,
      if (Seq(Partnership, LtdPartnership, ScotLtdPartnership, ScotPartnership).exists(scheme.partyType.contains)) Some(leadPartnerDetailsRow) else None,
      Some(VerifyBusinessTaskList.businessInfoRow)
    ).flatten
  )

  def leadPartnerDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = _ => "tasklist.aboutYou.leadPartnerDetails",
      url = _ => _ => controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url,
      tagId = "leadPartnerDetailsRow",
      checks = scheme => Seq(
        scheme.entities.exists(_.nonEmpty),
        scheme.entities.exists(_.headOption.exists(_.isModelComplete(isLeadPartner = true)))
      ),
      prerequisites = _ => Seq(VerifyBusinessTaskList.businessInfoRow)
    )
  }

  def addressDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = scheme => buildMessageKey("addressDetails", scheme),
      url = vatScheme => _ => resolveAddressRowUrl(vatScheme),
      tagId = "addressDetailsRow",
      checks = addressDetailsChecks,
      prerequisites = _ => Seq(personalDetailsRow)
    )
  }

  def contactDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = scheme => buildMessageKey("contactDetails", scheme),
      url = _ => _ => controllers.applicant.routes.CaptureEmailAddressController.show.url,
      tagId = "contactDetailsRow",
      checks = scheme => {
        Seq(
          scheme.applicantDetails.exists(_.contact.email.isDefined),
          scheme.applicantDetails.exists(_.contact.tel.isDefined)
        )
      }.++ {
        if (scheme.eligibilitySubmissionData.exists(_.isTransactor)) {
          Nil
        } else {
          Seq(scheme.applicantDetails.exists(_.contact.emailVerified.contains(true)))
        }
      },
      prerequisites = _ => Seq(addressDetailsRow)
    )
  }

  def buildLeadPartnerRow(vatScheme: VatScheme)(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): Option[TaskListSectionRow] = {
    vatScheme.partyType match {
      case Some(Partnership) | Some(LtdPartnership) | Some(ScotPartnership) | Some(ScotLtdPartnership) =>
        Some(leadPartnerDetailsRow.build(vatScheme))
      case _ =>
        None
    }
  }

  private def resolveAddressRowUrl(vatScheme: VatScheme): String = {
    vatScheme.partyType match {
      case Some(NETP) | Some(NonUkNonEstablished) if vatScheme.eligibilitySubmissionData.exists(!_.fixedEstablishmentInManOrUk) =>
        controllers.applicant.routes.InternationalHomeAddressController.show.url
      case _ =>
        controllers.applicant.routes.HomeAddressController.redirectToAlf.url
    }
  }

  private def addressDetailsChecks(scheme: VatScheme) = {
    Seq(
      Some(scheme.applicantDetails.exists(_.currentAddress.isDefined)),
      if (scheme.applicantDetails.exists(_.noPreviousAddress.contains(true))) {
        None
      } else {
        Some(scheme.applicantDetails.exists(_.previousAddress.isDefined))
      }
    ).flatten
  }

  def build(vatScheme: VatScheme)
           (implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = buildMessageKey("heading", vatScheme),
      rows = Seq(
        buildLeadPartnerRow(vatScheme),
        Some(personalDetailsRow.build(vatScheme)),
        Some(addressDetailsRow.build(vatScheme)),
        Some(contactDetailsRow.build(vatScheme))
      ).flatten
    )
}
