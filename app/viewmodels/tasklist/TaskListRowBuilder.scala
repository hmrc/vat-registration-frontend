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

import models.api.VatScheme

case class TaskListRowBuilder(messageKey: VatScheme => String,
                              url: VatScheme => String,
                              tagId: String,
                              checks: VatScheme => Seq[Boolean],
                              prerequisites: VatScheme => Seq[TaskListRowBuilder]) {

  def isComplete(vatScheme: VatScheme): Boolean = checks(vatScheme).forall(_ == true)

  def build(vatScheme: VatScheme): TaskListSectionRow = {
    def prerequisitesMet: Boolean = prerequisites(vatScheme).forall(_.isComplete(vatScheme))
    def partiallyCompleted: Boolean = !isComplete(vatScheme) && checks(vatScheme).contains(true)

    val status = if (prerequisitesMet) {
      if (isComplete(vatScheme)) {
        TLCompleted
      } else {
        if (partiallyCompleted) {
          TLInProgress
        } else {
          TLNotStarted
        }
      }
    } else {
      TLCannotStart
    }

    TaskListSectionRow(messageKey(vatScheme), url(vatScheme), tagId, status)
  }

}


