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
import models.api.VatScheme
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import views.html.components.TaskListComponent

import javax.inject.{Inject, Singleton}

@Singleton
class TaskListBuilder @Inject()(taskList: TaskListComponent,
                                registrationReasonSection: RegistrationReasonTaskList) {

  def build(vatScheme: VatScheme)
           (implicit request: Request[_], messages: Messages, appConfig: FrontendAppConfig): HtmlFormat.Appendable =
    taskList(registrationReasonSection.build(vatScheme))

}
