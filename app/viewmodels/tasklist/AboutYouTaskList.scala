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
import models.api.{LtdPartnership, Partnership, ScotLtdPartnership, ScotPartnership, VatScheme}
import play.api.i18n.Messages
import play.api.mvc.Request

import javax.inject.{Inject, Singleton}

@Singleton
class AboutYouTaskList @Inject()(verifyBusinessTaskList: VerifyBusinessTaskList) {

  val leadPartnerDetailsRow: TaskListRowBuilder = {
    TaskListRowBuilder(
      messageKey = "tasklist.aboutYou.leadPartnerDetails",
      url =
        _ => controllers.applicant.routes.LeadPartnerEntityController.showLeadPartnerEntityType.url,
      tagId = "leadPartnerDetailsRow",
      checks =
        scheme => Seq(scheme.partners.exists(_.exists(_.isLeadPartner))),
      prerequisites =
        _ => Seq(verifyBusinessTaskList.businessInfoRow)
    )
  }

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.aboutYou.heading"),
      rows = Seq(buildLeadPartnerRow(vatScheme)).flatten
    )

  def buildLeadPartnerRow(vatScheme: VatScheme): Option[TaskListSectionRow] = {
    vatScheme.partyType match {
      case Some(Partnership) | Some(LtdPartnership) | Some(ScotPartnership) | Some(ScotLtdPartnership) =>
        Some(leadPartnerDetailsRow.build(vatScheme))
      case _ =>
        None
    }
  }
}
