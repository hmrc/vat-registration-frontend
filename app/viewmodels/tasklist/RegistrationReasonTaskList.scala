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
import models.api.VatScheme
import play.api.i18n.Messages

import javax.inject.Singleton

@Singleton
object RegistrationReasonTaskList {

  def registrationReasonRow(regId: String)(implicit appConfig: FrontendAppConfig): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.eligibilty.regReason",
    url = _ => _ => appConfig.eligibilityStartUrl(regId),
    tagId = "regReasonRow",
    checks = scheme => Seq(
      scheme.eligibilitySubmissionData.isDefined
    ),
    prerequisites = _ => Seq()
  )

  def build(vatScheme: VatScheme)(implicit messages: Messages, appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.eligibility.heading"),
      rows = Seq(registrationReasonRow(vatScheme.registrationId).build(vatScheme))
    )

}
