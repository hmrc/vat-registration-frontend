/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.builders

import helpers.VatRegSpec
import models.view.{SummaryRow, SummarySection}
import play.api.mvc.Call

class SummarySectionBuilderSpec extends VatRegSpec {

  object TestBuilder extends SummarySectionBuilder {
    override val sectionId: String = "section"
    override val section: SummarySection = SummarySection(id = "test", rows = Seq.empty)
  }

  val testCall = Call("GET", "someUrl")

  "yes or no row" should {
    "display Yes" when {
      "user answered Yes to a question" in {
        TestBuilder.yesNoRow("row", Some(true), testCall) mustBe
          SummaryRow("section.row", "app.common.yes", Some(testCall))
      }
    }

    "display No" when {
      "user answered No to a question" in {
        TestBuilder.yesNoRow("row", Some(false), testCall) mustBe
          SummaryRow("section.row", "app.common.no", Some(testCall))
      }
    }

    "display No" when {
      "user did not answer" in {
        TestBuilder.yesNoRow("row", None, testCall) mustBe
          SummaryRow("section.row", "app.common.no", Some(testCall))
      }
    }

  }

  "applied row" should {
    "display Applied" when {
      "user answered Yes to a question" in {
        TestBuilder.appliedRow("row", Some(true), testCall) mustBe
          SummaryRow("section.row", "app.common.applied", Some(testCall))
      }
    }

    "display No" when {
      "user answered No to a question" in {
        TestBuilder.appliedRow("row", Some(false), testCall) mustBe
          SummaryRow("section.row", "app.common.not.applied", Some(testCall))
      }
    }

    "display No" when {
      "user did not answer" in {
        TestBuilder.appliedRow("row", None, testCall) mustBe
          SummaryRow("section.row", "app.common.not.applied", Some(testCall))
      }
    }

  }

}
