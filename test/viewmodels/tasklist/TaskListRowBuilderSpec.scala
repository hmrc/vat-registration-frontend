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

import fixtures.VatRegistrationFixture
import play.api.i18n.{Lang, Messages}
import views.VatRegViewSpec
import views.html.components.TaskListRow

class TaskListRowBuilderSpec extends VatRegViewSpec with VatRegistrationFixture {

  val rowComponent: TaskListRow = app.injector.instanceOf[TaskListRow]

  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val prereqMessageKey = "prerequisite"
  val prereqAriaLabel = "prereq aria"
  val rowMessageKey = "test row"
  val rowAriaLabel = "test row aria"
  val testUrl = "testUrl"

  def testPrerequisite(checksPass: Boolean): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => prereqMessageKey,
    url = _ => _ => testUrl,
    tagId = prereqAriaLabel,
    checks = _ => Seq(checksPass),
    prerequisites = _ => Seq()
  )

  def testRow(prerequisitesMet: Boolean): TaskListRowBuilder = TaskListRowBuilder(
    messageKey = _ => rowMessageKey,
    url = _ => _ => testUrl,
    tagId = rowAriaLabel,
    checks = scheme => Seq(
      scheme.business.isDefined,
      scheme.applicantDetails.map(_.personalDetails).isDefined
    ),
    prerequisites = _ => Seq(testPrerequisite(prerequisitesMet))
  )

  "build" when {
    "all prerequisites have been met" when {
      "all required rows are complete" must {
        "return COMPLETED" in {
          val testVatScheme = emptyVatScheme.copy(business = Some(validBusiness), applicantDetails = Some(completeApplicantDetails))

          val res = testRow(prerequisitesMet = true).build(testVatScheme)

          res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLCompleted)
        }
      }
      "some required rows are not finished" must {
        "return IN PROGRESS" in {
          val testVatScheme = emptyVatScheme.copy(business = Some(validBusiness), applicantDetails = None)

          val res = testRow(prerequisitesMet = true).build(testVatScheme)

          res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLInProgress)
        }
      }
      "all required rows are marked as incomplete" must {
        "return INCOMPLETE" in {
          val testVatScheme = emptyVatScheme.copy(business = Some(validBusiness), applicantDetails = Some(completeApplicantDetails))

          val incompleteRow = TaskListRowBuilder(
            messageKey = _ => rowMessageKey,
            url = _ => _ => testUrl,
            tagId = rowAriaLabel,
            checks = scheme => Seq(
              scheme.business.isDefined,
              scheme.applicantDetails.map(_.personalDetails).isDefined
            ),
            prerequisites = _ => Seq(testPrerequisite(true)),
            incomplete = _ => true
          )

          val res = incompleteRow.build(testVatScheme)

          res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLInComplete)
        }
      }
      "no required rows are complete" must {
        "return NOT STARTED" in {
          val testVatScheme = emptyVatScheme

          val res = testRow(prerequisitesMet = true).build(testVatScheme)

          res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLNotStarted)
        }
      }
    }

    "prerequisites haven't been met" must {
      "return CANNOT START" in {
        val testVatScheme = emptyVatScheme

        val res = testRow(prerequisitesMet = false).build(testVatScheme)

        res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLCannotStart)
      }
    }

    "current prerequisite met but chained prerequisite hasn't been met" must {
      "return CANNOT START" in {
        val chainedPrerequisite = testPrerequisite(checksPass = true).copy(prerequisites = _ => Seq(testPrerequisite(checksPass = false)))
        val result = testPrerequisite(checksPass = true).copy(prerequisites = _ => Seq(chainedPrerequisite)).build(emptyVatScheme)

        result.status mustBe TLCannotStart
      }
    }
  }

  "TaskListSection isReadyForSubmission" when {
    "all rows are Completed" must {
      "return true" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLCompleted),
          TaskListSectionRow("row2", testUrl, "tag2", TLCompleted)
        )).isReadyForSubmission mustBe true
      }
    }

    "all rows are Incomplete" must {
      "return true" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLInComplete)
        )).isReadyForSubmission mustBe true
      }
    }

    "rows are a mix of Completed and Incomplete" must {
      "return true" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLCompleted),
          TaskListSectionRow("row2", testUrl, "tag2", TLInComplete)
        )).isReadyForSubmission mustBe true
      }
    }

    "any row is NotStarted" must {
      "return false" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLCompleted),
          TaskListSectionRow("row2", testUrl, "tag2", TLNotStarted)
        )).isReadyForSubmission mustBe false
      }
    }

    "any row is CannotStart" must {
      "return false" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLCompleted),
          TaskListSectionRow("row2", testUrl, "tag2", TLCannotStart)
        )).isReadyForSubmission mustBe false
      }
    }

    "any row is InProgress" must {
      "return false" in {
        TaskListSection("heading", List(
          TaskListSectionRow("row1", testUrl, "tag1", TLCompleted),
          TaskListSectionRow("row2", testUrl, "tag2", TLInProgress)
        )).isReadyForSubmission mustBe false
      }
    }
  }
}