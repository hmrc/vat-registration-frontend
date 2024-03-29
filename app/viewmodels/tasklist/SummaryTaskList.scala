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
import models.api.VatScheme
import play.api.i18n.Messages
import services.BusinessService

import javax.inject.Inject

object SummaryTaskList {

  def summaryRow(attachmentsTaskListRowBuilder: Option[TaskListRowBuilder], businessService: BusinessService)
                (implicit profile: CurrentProfile,
                 appConfig: FrontendAppConfig): TaskListRowBuilder = {

    TaskListRowBuilder(
      messageKey = _ => "tasklist.vatRegistration.cya.submit",
      url= _ => _ => controllers.routes.SummaryController.show.url,
      tagId = "summaryRow",
      checks = _ => Seq(false),
      prerequisites = vatScheme => Seq(
        attachmentsTaskListRowBuilder.getOrElse(
          VatRegistrationTaskList.resolveFlatRateSchemeRow(vatScheme, businessService).getOrElse(
            VatRegistrationTaskList.vatReturnsRow(businessService)
          )
        )
      )
    )
  }

  def build(vatScheme: VatScheme, attachmentsTaskListRowBuilder: Option[TaskListRowBuilder], businessService: BusinessService)
           (implicit profile: CurrentProfile, messages: Messages, appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.cya.heading"),
      rows = Seq(summaryRow(attachmentsTaskListRowBuilder, businessService).build(vatScheme))
    )
}