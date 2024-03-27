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
import play.api.mvc.Request
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

case class TaskListSection(heading: String, rows: Seq[TaskListSectionRow]) {

  def isComplete: Boolean = rows.forall(_.status == TLCompleted)

}

object TaskListSections {


  def sections(vatScheme: VatScheme, businessService: BusinessService, attachmentsRequiredRow: Option[TaskListRowBuilder])(implicit messagesApi: Messages, appConfig: FrontendAppConfig, profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]) = List(
    Some(RegistrationReasonTaskList.build(vatScheme)),
    if (vatScheme.eligibilitySubmissionData.exists(_.isTransactor)) Some(AboutYouTransactorTaskList.build(vatScheme)) else None,
    Some(VerifyBusinessTaskList.build(vatScheme)),
    Some(AboutYouTaskList.build(vatScheme)),
    Some(AboutTheBusinessTaskList.build(vatScheme, businessService)),
    Some(VatRegistrationTaskList.build(vatScheme, businessService)),
    attachmentsRequiredRow.map(AttachmentsTaskList.build(vatScheme, _))
  ).flatten
  def allComplete(vatScheme: VatScheme, businessService: BusinessService, attachmentsRequiredRow: Option[TaskListRowBuilder])
                 (implicit messagesApi: Messages, appConfig: FrontendAppConfig, profile: CurrentProfile, hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Boolean =
    sections(vatScheme, businessService, attachmentsRequiredRow).forall{x =>x.isComplete}
}

case class TaskListSectionRow(messageKey: String,
                              url: String,
                              tagId: String,
                              status: TaskListState,
                              canEdit: Boolean = false)

sealed trait TaskListState

case object TLCannotStart extends TaskListState

case object TLNotStarted extends TaskListState

case object TLInProgress extends TaskListState

case object TLCompleted extends TaskListState

case object TLFailed extends TaskListState
