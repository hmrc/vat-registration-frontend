/*
 * Copyright 2020 HM Revenue & Customs
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

package viewmodels

import java.time.format.DateTimeFormatter

import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call

trait SummarySectionBuilder {

  val sectionId: String
  val section: SummarySection
  val presentationFormatter = DateTimeFormatter.ofPattern("d MMMM y")

  def yesNoRow
  (rowId: String, yesNo: Option[Boolean], changeLink: Call, trueKey: String = "app.common.yes", falseKey: String = "app.common.no") =
    SummaryRow(
      s"$sectionId.$rowId",
      if (yesNo.contains(true)) trueKey else falseKey,
      Some(changeLink)
    )

  def appliedRow(rowId: String, yesNo: Option[Boolean], changeLinkUrl: Call) =
    yesNoRow(rowId, yesNo, changeLinkUrl, "app.common.applied", "app.common.not.applied")

}
