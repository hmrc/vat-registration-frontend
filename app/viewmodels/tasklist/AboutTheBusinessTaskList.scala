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
import featureswitch.core.config.{FeatureSwitching, LandAndProperty, OtherBusinessInvolvement => OBI_FS}
import models.api.VatScheme
import models.{Business, CurrentProfile, OtherBusinessInvolvement}
import play.api.i18n.Messages
import play.api.mvc.Request
import services.BusinessService

import javax.inject.{Inject, Singleton}

@Singleton
class AboutTheBusinessTaskList @Inject()(aboutYouTaskList: AboutYouTaskList, businessService: BusinessService) extends FeatureSwitching {

  def businessDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.businessDetails",
    url = _ => controllers.routes.TradingNameResolverController.resolve.url,
    tagId = "businessDetailsRow",
    checks = scheme => {
      Seq(
        scheme.business.exists(_.ppobAddress.isDefined),
        scheme.business.exists(_.telephoneNumber.isDefined),
        scheme.business.exists(_.email.isDefined),
        scheme.business.exists(_.hasWebsite.isDefined),
        scheme.business.exists(_.contactPreference.isDefined)
      ).++ {
        if (scheme.business.exists(_.hasWebsite.contains(true))) {
          Seq(scheme.business.exists(_.website.isDefined))
        } else {
          Nil
        }
      }.++ {
        if (scheme.business.exists(_.hasTradingName.contains(true)) ||
          !scheme.partyType.exists(Business.tradingNameOptional)) {
          Seq(scheme.business.exists(_.tradingName.isDefined))
        } else {
          Nil
        }
      }
    },
    prerequisites = _ => Seq(aboutYouTaskList.contactDetailsRow)
  )

  def businessActivitiesRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.businessActivities",
    url = _ =>
      if (isEnabled(LandAndProperty)) {
        controllers.business.routes.LandAndPropertyController.show.url
      } else {
        controllers.business.routes.BusinessActivityDescriptionController.show.url
      },
    tagId = "businessActivitiesRow",
    checks = scheme => Seq(
      scheme.business.exists(_.businessDescription.isDefined),
      scheme.business.exists(_.mainBusinessActivity.isDefined)
    ).++ {
      val needsCompliance = scheme.business.flatMap(_.businessActivities).fold(false) { sicCodes =>
        businessService.needComplianceQuestions(sicCodes)
      }
      if (needsCompliance) {
        Seq(scheme.business.exists(_.labourCompliance.exists(businessService.isLabourComplianceModelComplete)))
      } else {
        Nil
      }
    }.++ {
      if (isEnabled(LandAndProperty)) {
        Seq(scheme.business.exists(_.hasLandAndProperty.isDefined))
      } else {
        Nil
      }
    },
    prerequisites = _ => Seq(businessDetailsRow)
  )

  def otherBusinessInvolvementsRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.otherBusinessInvolvements",
    url = scheme => resolveOtherBusinessInvolvementsUrl(scheme),
    tagId = "otherBusinessInvolvementsRow",
    checks = scheme => {
      val firstQuestionAnswered = Seq(scheme.business.exists(_.otherBusinessInvolvement.isDefined))
      if (scheme.business.exists(_.otherBusinessInvolvement.contains(true))) {
        firstQuestionAnswered :+
          !(scheme.otherBusinessInvolvements match {
            case Some(Nil) | None => List(false)
            case Some(list) => list.map {
              case OtherBusinessInvolvement(Some(_), Some(true), Some(_), _, _, Some(_)) => true
              case OtherBusinessInvolvement(Some(_), _, _, Some(true), Some(_), Some(_)) => true
              case OtherBusinessInvolvement(Some(_), Some(false), _, Some(false), _, Some(_)) => true
              case _ => false
            }
          }).contains(false)
      } else {
        firstQuestionAnswered
      }
    },
    prerequisites = _ => Seq(businessActivitiesRow)
  )

  private def resolveOtherBusinessInvolvementsUrl(vatScheme: VatScheme): String = {
    vatScheme.otherBusinessInvolvements match {
      case Some(Nil) | None => controllers.otherbusinessinvolvements.routes.OtherBusinessInvolvementController.show.url
      case _ => controllers.otherbusinessinvolvements.routes.ObiSummaryController.show.url
    }
  }

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.aboutTheBusiness.heading"),
      rows = Seq(
        businessDetailsRow.build(vatScheme),
        businessActivitiesRow.build(vatScheme)
      ).++ {
        if (isEnabled(OBI_FS)) Seq(otherBusinessInvolvementsRow.build(vatScheme)) else Nil
      }
    )
}
