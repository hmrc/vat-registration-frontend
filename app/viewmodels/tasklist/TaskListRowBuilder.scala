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

import models.api.VatScheme

import scala.annotation.tailrec

case class TaskListRowBuilder(messageKey: VatScheme => String,
                              url: VatScheme => TaskListState => String,
                              tagId: String,
                              checks: VatScheme => Seq[Boolean],
                              prerequisites: VatScheme => Seq[TaskListRowBuilder],
                              error: VatScheme => Boolean = _ => false,
                              canEdit: TaskListState => Boolean = _ => false) {

  def isComplete(vatScheme: VatScheme): Boolean = checks(vatScheme).forall(_ == true)

  def prerequisitesMet(vatScheme: VatScheme): Boolean = {
    @tailrec
    def checkCompleteness(rows: Seq[TaskListRowBuilder], result: Boolean = true): Boolean = {
      rows match {
        case Nil => result
        case row :: tail => checkCompleteness(tail ++ row.prerequisites(vatScheme), result && row.isComplete(vatScheme))
      }
    }

    checkCompleteness(prerequisites(vatScheme))
  }

  def build(vatScheme: VatScheme): TaskListSectionRow = {
    val status = if (prerequisitesMet(vatScheme)) {
      if(error(vatScheme)) {
        TLFailed
      } else if (isComplete(vatScheme)) {
        TLCompleted
      } else  if (checks(vatScheme).contains(true)) {
          TLInProgress
        } else {
          TLNotStarted
        }
    } else {
      TLCannotStart
    }

    TaskListSectionRow(messageKey(vatScheme), url(vatScheme)(status), tagId, status, canEdit(status))
  }
}