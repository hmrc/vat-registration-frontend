/*
 * Copyright 2023 HM Revenue & Customs
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

package views.attachments

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import viewmodels.tasklist.{TLCompleted, TLFailed, TLInProgress, TLNotStarted, TaskListSectionRow}
import views.VatRegViewSpec
import views.html.attachments.UploadDocumentsNewJourney

import scala.collection.JavaConverters.asScalaBufferConverter

class UploadSummaryViewSpec extends VatRegViewSpec {

  val view: UploadDocumentsNewJourney = app.injector.instanceOf[UploadDocumentsNewJourney]
  val taskListRows: List[TaskListSectionRow] = List(
    TaskListSectionRow("VAT2", "url", "tagId", TLNotStarted),
    TaskListSectionRow("VAT51", "url", "tagId", TLInProgress),
    TaskListSectionRow("Identity Evidence", "url", "tagId", TLFailed, canEdit = true),
    TaskListSectionRow("Tax Representative Authorisation", "url", "tagId", TLCompleted, canEdit = true)
  )

  val expectedHeading = "Upload your documents"
  val expectedItems: List[String] = List("VAT2", "Not started", "VAT51", "In progress", "Identity Evidence", "Edit", "Failed", "Tax Representative Authorisation", "Edit", "Completed")

  "The upload summary page" must {
    implicit val doc: Document = Jsoup.parse(view(taskListRows: _*).body)

    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct title" in new ViewSetup {
      doc.heading mustBe Some(expectedHeading)
    }

    "have the correct list of attachments" in new ViewSetup {
        val taskListElements = doc.select(".app-task-list__row td")
        taskListElements.eachText().asScala.toList mustBe expectedItems
    }
  }
}
