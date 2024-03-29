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

  val rowComponent = app.injector.instanceOf[TaskListRow]

  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  val prereqMessageKey = "prerequisite"
  val prereqAriaLabel = "prereq aria"
  val rowMessageKey = "test row"
  val rowAriaLabel = "test row aria"
  val testUrl = "testUrl"

  def testPrerequisite(checksPass: Boolean) = TaskListRowBuilder(
    messageKey = _ => prereqMessageKey,
    url = _ => _ => testUrl,
    tagId = prereqAriaLabel,
    checks = _ => Seq(checksPass),
    prerequisites = _ => Seq()
  )

  def testRow(prerequisitesMet: Boolean) = TaskListRowBuilder(
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
      "some required rows are incomplete" must {
        "return IN PROGRESS" in {
          val testVatScheme = emptyVatScheme.copy(business = Some(validBusiness), applicantDetails = None)

          val res = testRow(prerequisitesMet = true).build(testVatScheme)

          res mustBe TaskListSectionRow(rowMessageKey, testUrl, rowAriaLabel, TLInProgress)
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
}