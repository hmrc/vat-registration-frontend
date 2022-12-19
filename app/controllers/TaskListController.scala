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

package controllers

import config.{BaseControllerComponents, FrontendAppConfig}
import play.api.mvc.{Action, AnyContent}
import services._
import uk.gov.hmrc.auth.core.AuthConnector
import viewmodels.tasklist._
import views.html.TaskList

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class TaskListController @Inject()(vatRegistrationService: VatRegistrationService,
                                   val authConnector: AuthConnector,
                                   val sessionService: SessionService,
                                   registrationReasonSection: RegistrationReasonTaskList,
                                   aboutYouTransactorTaskList: AboutYouTransactorTaskList,
                                   verifyBusinessTaskList: VerifyBusinessTaskList,
                                   aboutYouTaskList: AboutYouTaskList,
                                   aboutTheBusinessTaskList: AboutTheBusinessTaskList,
                                   vatRegistrationTaskList: VatRegistrationTaskList,
                                   attachmentsTaskList: AttachmentsTaskList,
                                   summaryTaskList: SummaryTaskList,
                                   businessService: BusinessService,
                                   vatApplicationService: VatApplicationService,
                                   view: TaskList)
                                  (implicit val executionContext: ExecutionContext,
                                   bcc: BaseControllerComponents,
                                   appConfig: FrontendAppConfig) extends BaseController {


  def show(): Action[AnyContent] = isAuthenticatedWithProfile { implicit request => implicit profile =>
    for {
      vatScheme <- vatRegistrationService.getVatScheme
      attachmentsTaskListRow <- attachmentsTaskList.attachmentsRequiredRow
      sections = List(
        Some(registrationReasonSection.build(vatScheme)),
        if (vatScheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(aboutYouTransactorTaskList.build(vatScheme)) else None,
        Some(verifyBusinessTaskList.build(vatScheme)),
        Some(aboutYouTaskList.build(vatScheme)),
        Some(aboutTheBusinessTaskList.build(vatScheme)),
        Some(vatRegistrationTaskList.build(vatScheme)),
        attachmentsTaskListRow.map(attachmentsTaskList.build(vatScheme, _)),
        Some(summaryTaskList.build(vatScheme, attachmentsTaskListRow))
      ).flatten
    } yield Ok(view(sections: _*))
  }

}
