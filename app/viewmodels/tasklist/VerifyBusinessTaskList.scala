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
import models.CurrentProfile
import models.api.VatScheme
import play.api.i18n.Messages
import play.api.mvc.Request
import services.S4LService

import javax.inject.{Inject, Singleton}

@Singleton
class VerifyBusinessTaskList @Inject()(registrationReasonTaskList: RegistrationReasonTaskList,
                                       aboutYouTransactorTaskList: AboutYouTransactorTaskList) {

  def businessInfoRow(implicit profile: CurrentProfile) = TaskListRowBuilder(
    messageKey = _ => "tasklist.verifyBusiness.businessInfo",
    url =
      _ => controllers.routes.BusinessIdentificationResolverController.resolve.url,
    tagId = "verifyBusinessRow",
    checks =
      scheme => Seq(scheme.applicantDetails.exists(_.entity.isDefined)),
    prerequisites = scheme =>
      Seq(
        Some(registrationReasonTaskList.registrationReasonRow(scheme.id)),
        if (scheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(aboutYouTransactorTaskList.transactorPersonalDetailsRow) else None
      ).flatten
  )

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.verifyBusiness.heading"),
      rows = Seq(businessInfoRow.build(vatScheme))
    )

}
