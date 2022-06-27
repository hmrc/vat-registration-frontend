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

case class TaskListSection(heading: String, rows: Seq[TaskListSectionRow]) {

  def isComplete: Boolean = rows.forall(_.status == TLCompleted)

}

case class TaskListSectionRow(messageKey: String,
                              url: String,
                              tagId: String,
                              status: TaskListState)

sealed trait TaskListState

case object TLCannotStart extends TaskListState

case object TLNotStarted extends TaskListState

case object TLInProgress extends TaskListState

case object TLCompleted extends TaskListState
