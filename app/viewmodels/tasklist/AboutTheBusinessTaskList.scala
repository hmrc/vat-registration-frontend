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

import javax.inject.{Inject, Singleton}

@Singleton
class AboutTheBusinessTaskList @Inject()(aboutYouTaskList: AboutYouTaskList) {

  def businessDetailsRow(implicit profile: CurrentProfile): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => "tasklist.aboutTheBusiness.businessDetails",
    url = _ => controllers.routes.TradingNameResolverController.resolve.url,
    tagId = "businessDetailsRow",
    checks = scheme => {
      Seq(
        scheme.tradingDetails.exists(_.tradingNameView.isDefined),
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
      }
    },
    prerequisites = _ =>
      Seq(aboutYouTaskList.contactDetailsRow)
  )

  def build(vatScheme: VatScheme)
           (implicit request: Request[_],
            profile: CurrentProfile,
            messages: Messages,
            appConfig: FrontendAppConfig): TaskListSection =
    TaskListSection(
      heading = messages("tasklist.aboutTheBusiness.heading"),
      rows = Seq(businessDetailsRow.build(vatScheme))
    )

}
