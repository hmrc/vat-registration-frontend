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

package viewmodels.tasklist

import config.FrontendAppConfig
import controllers.partners.PartnerIndexValidation.minPartnerIndex
import models.api._
import models.external.{MinorEntity, PartnershipIdEntity}
import models.{Business, CurrentProfile}
import play.api.i18n.Messages
import services.BusinessService

import javax.inject.{Inject, Singleton}

@Singleton
object AboutTheBusinessTaskList {

  def build(vatScheme: VatScheme, businessService: BusinessService)
           (implicit profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.aboutTheBusiness.heading"),
      rows = Seq(
        buildPartnersDetailRow(vatScheme).map(_.build(vatScheme)),
        Some(businessDetailsRow.build(vatScheme)),
        Some(businessActivitiesRow(businessService).build(vatScheme)),
        Some(otherBusinessInvolvementsRow(businessService).build(vatScheme))
      ).flatten
    )

  //scalastyle:off
  def buildPartnersDetailRow(vatScheme: VatScheme)(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): Option[TaskListRowBuilder] = {
    vatScheme.partyType match {
      case Some(Partnership) | Some(LtdPartnership) | Some(ScotPartnership) | Some(ScotLtdPartnership) =>
        Some(
          TaskListRowBuilder(
            messageKey = _ => "tasklist.aboutTheBusiness.partnersDetail",
            url = _ => _ => {
              if (vatScheme.entities.exists(_.size < minPartnerIndex))
                controllers.partners.routes.PartnerEntityTypeController.showPartnerType(minPartnerIndex).url
              else
                controllers.partners.routes.PartnerSummaryController.show.url
            },
            tagId = "partnersDetailRow",
            checks = scheme => Seq(
              scheme.entities.exists(_.size > 1),
              scheme.entities.exists(_.filter(_.isLeadPartner.contains(false)).forall(_.isModelComplete(isLeadPartner = false))) && scheme.entities.exists(_.size > 1),
              (scheme.attachments.exists(_.additionalPartnersDocuments.contains(true)) && scheme.entities.exists(_.size == appConfig.maxPartnerCount))
                || (scheme.attachments.exists(_.additionalPartnersDocuments.contains(false)) && scheme.entities.exists(_.size > 1))
            ),
            prerequisites = _ => Seq(AboutYouTaskList.contactDetailsRow)
          )
        )
      case _ =>
        None
    }
  }

  def businessDetailsRow(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.businessDetails",
    url = _ => _ => controllers.routes.TradingNameResolverController.resolve.url,
    tagId = "businessDetailsRow",
    checks = scheme => {
      Seq(
        scheme.business.exists(_.ppobAddress.isDefined),
        scheme.business.exists(_.telephoneNumber.isDefined),
        scheme.business.exists(_.email.isDefined),
        scheme.business.exists(_.hasWebsite.isDefined),
        scheme.business.exists(_.welshLanguage.isDefined),
        scheme.business.exists(_.contactPreference.isDefined)
      ) ++ {
        if (scheme.business.exists(_.hasWebsite.contains(true))) {
          Seq(scheme.business.exists(_.website.isDefined))
        } else {
          Nil
        }
      } ++ {
        if (scheme.business.exists(_.hasTradingName.contains(false)) ||
          !scheme.partyType.exists(Business.tradingNameOptional)) {
          Seq(scheme.business.exists(_.tradingName.isDefined))
        } else {
          Nil
        }
      } ++ {
        // Unincorporated entities have to go through the company/partnership name pages which are handled by the TradingNameResolverController
        scheme.applicantDetails.flatMap(_.entity) match {
          case Some(businessEntity: PartnershipIdEntity) if Seq(Partnership, ScotPartnership).exists(scheme.partyType.contains) =>
            Seq(businessEntity.companyName.isDefined)
          case Some(businessEntity: MinorEntity) =>
            Seq(businessEntity.companyName.isDefined)
          case _ => Nil
        }
      }
    },
    prerequisites = vatScheme => {
      Seq(buildPartnersDetailRow(vatScheme).getOrElse(AboutYouTaskList.contactDetailsRow))
    }
  )

  def businessActivitiesRow(businessService: BusinessService)(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.businessActivities",
    url = _ => _ => controllers.business.routes.LandAndPropertyController.show.url,
    tagId = "businessActivitiesRow",
    checks = scheme => Seq(
      scheme.business.exists(_.businessDescription.isDefined),
      scheme.business.exists(_.mainBusinessActivity.isDefined),
      scheme.business.exists(_.hasLandAndProperty.isDefined)
    ).++ {
      val needsCompliance = scheme.business.flatMap(_.businessActivities).fold(false) { sicCodes =>
        businessService.needComplianceQuestions(sicCodes)
      }
      if (needsCompliance) {
        Seq(scheme.business.exists(_.labourCompliance.exists(businessService.isLabourComplianceModelComplete)))
      } else {
        Nil
      }
    },
    prerequisites = _ => Seq(businessDetailsRow)
  )

  def otherBusinessInvolvementsRow(businessService: BusinessService)(implicit profile: CurrentProfile, appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.otherBusinessInvolvements",
    url = scheme => _ => scheme.otherBusinessInvolvements match {
      case Some(obiList) if obiList.exists(_.isModelComplete) => controllers.otherbusinessinvolvements.routes.ObiSummaryController.show.url
      case _ => controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
    },
    tagId = "otherBusinessInvolvementsRow",
    checks = scheme => {
      if (scheme.business.exists(_.otherBusinessInvolvement.contains(true))) {
        Seq(
          scheme.business.exists(_.otherBusinessInvolvement.isDefined),
          scheme.otherBusinessInvolvements.exists(_.nonEmpty),
          scheme.otherBusinessInvolvements.exists(_.forall(_.isModelComplete))
        )
      } else {
        Seq(scheme.business.exists(_.otherBusinessInvolvement.isDefined))
      }
    },
    prerequisites = _ => Seq(businessActivitiesRow(businessService))
  )

}
