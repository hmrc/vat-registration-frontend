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
import featureswitch.core.config.{FeatureSwitching, FullAgentJourney, UseSoleTraderIdentification}
import models.CurrentProfile
import models.api.{UkCompany, VatScheme}
import play.api.i18n.Messages
import play.api.mvc.Request

import javax.inject.Inject

class AboutYouTransactorTaskList @Inject()(registrationReasonTaskList: RegistrationReasonTaskList) extends FeatureSwitching {

  def transactorPersonalDetailsRow(implicit profile: CurrentProfile) = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutYou.personalDetails",
    url = _ => {
      if (profile.agentReferenceNumber.isDefined && isEnabled(FullAgentJourney)) {
        controllers.transactor.routes.AgentNameController.show.url
      } else {
        controllers.transactor.routes.PartOfOrganisationController.show.url
      }
    },
    tagId = "transactorPersonalDetailsRow",
    checks = scheme => {
      if (profile.agentReferenceNumber.isDefined && isEnabled(FullAgentJourney)) {
        Seq(scheme.transactorDetails.exists(_.personalDetails.isDefined))
      }
      else {
        Seq(
          scheme.transactorDetails.exists(_.personalDetails.isDefined),
          scheme.transactorDetails.exists(_.isPartOfOrganisation.isDefined),
          if (scheme.transactorDetails.exists(_.isPartOfOrganisation.contains(true))) {
            scheme.transactorDetails.exists(_.organisationName.isDefined)
          } else {
            scheme.transactorDetails.exists(_.declarationCapacity.isDefined)
          }
        )
      }
    },
    prerequisites = scheme => Seq(registrationReasonTaskList.registrationReasonRow(scheme.id))
  )

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.aboutYou.heading"),
      rows = Seq(
        transactorPersonalDetailsRow.build(vatScheme)
      )
    )

}
