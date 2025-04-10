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

package views

import featuretoggle.FeatureSwitch.SubmitDeadline
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import viewmodels.tasklist.TaskListSections.sections
import viewmodels.tasklist.{TLCompleted, TaskListSection, TaskListSectionRow}
import views.html.TaskList
import views.html.helper.form

import scala.collection.JavaConverters._

class TaskListViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[TaskList]
  val testUrl = "/testUrl"

  def sectionMustExist(n: Int)(heading: String, rows: List[String])(implicit doc: Document) = {
    val sectionHeadingSelector = ".app-task-list__section"
    val rowSelector = "ul.app-task-list__items"

    doc.select(sectionHeadingSelector).eachText().asScala.toList.lift(n - 1) mustBe Some(heading)
    val items = doc.select(rowSelector).asScala.toList.apply(n - 1)
    items.select("li").eachText().asScala.toList mustBe rows
  }

  object ExpectedMessages {
    val h1 = "Register for VAT"

    val bannerHeading = "You must complete and submit this VAT registration application by 19 May 2025."
    val bannerText = "Otherwise, you’ll have to start the application again."

    val section1 = new {
      val heading = "section 1 heading"
      val row1 = "section 1 row 1 message"
      val row2 = "section 1 row 2 message"
    }

    val section2 = new {
      val heading = "section 2 heading"
      val row1 = "section 2 row 1 message"
      val row2 = "section 2 row 2 message"
    }
  }

  val row1 = TaskListSectionRow(
    messageKey = ExpectedMessages.section1.row1,
    url = testUrl,
    tagId = "row1",
    TLCompleted
  )

  val row2 = row1.copy(messageKey = ExpectedMessages.section1.row2)
  val row3 = row1.copy(messageKey = ExpectedMessages.section2.row1)
  val row4 = row1.copy(messageKey = ExpectedMessages.section2.row2)

  implicit val doc = Jsoup.parse(view(
    TaskListSection(
      heading = ExpectedMessages.section1.heading,
      rows = List(
        row1,
        row2
      )
    ),
    TaskListSection(
      heading = ExpectedMessages.section2.heading,
      rows = List(
        row3,
        row4
      )
    )
  ).body)

  "the task list page" must {
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.h1)
    }
    "render all sections" in new ViewSetup {
      sectionMustExist(1)(s"1. ${ExpectedMessages.section1.heading}", List(s"${ExpectedMessages.section1.row1} Completed", s"${ExpectedMessages.section1.row2} Completed"))
      sectionMustExist(2)(s"2. ${ExpectedMessages.section2.heading}", List(s"${ExpectedMessages.section2.row1} Completed", s"${ExpectedMessages.section2.row2} Completed"))
    }
    "have a banner message when TTMayJunJourney is Enabled" in new ViewSetup {
      appConfig.setValue(SubmitDeadline, "true")
      doc.headingLevel3 mustBe Some(ExpectedMessages.bannerHeading)
      doc.para(1) mustBe Some(ExpectedMessages.bannerText)
    }
  }
}
