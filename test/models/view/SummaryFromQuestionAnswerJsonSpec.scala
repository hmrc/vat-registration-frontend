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

package models.view

import fixtures.VatRegistrationFixture
import play.api.libs.json.{JsSuccess, Json}
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class SummaryFromQuestionAnswerJsonSpec extends VatRegSpec with VatRegistrationFixture {

  def eligibilityCall(uri: String): String = s"http://vatRegEFEUrl/question?pageId=$uri"

  val changeText = "Change"

  "summaryReads" should {
    "return a summary with from Full Json" in {
      val res = Json.fromJson[SummaryList](fullEligibilityDataJson)(
        EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, changeText)
      )

      res.map(_.rows.length).get mustBe 8
    }

    "one section exists with questionId's with dashes in. all dashes and characters after the dash are removed for change links" in {
      val eligibilityJsonWithQuestionIdDashes = Json.parse(
        """
          |{ "sections": [
          |            {
          |              "title": "section_1",
          |              "data": [
          |                {"questionId": "mandatoryRegistration-foo", "question": "Question 1", "answer": "FOO", "answerValue": true},
          |                {"questionId": "voluntaryRegistration", "question": "Question 2", "answer": "BAR", "answerValue": false},
          |                {"questionId": "thresholdPreviousThirtyDays-", "question": "Question 3", "answer": "wizz", "answerValue": "2017-5-23"},
          |                {"questionId": "thresholdInTwelveMonths-foo-bar", "question": "Question 4", "answer": "woosh", "answerValue": "2017-7-16"}
          |              ]
          |}]}""".stripMargin
      )

      val res = Json.fromJson[SummaryList](eligibilityJsonWithQuestionIdDashes)(
        EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, changeText)
      )

      res.map(_.rows.length).get mustBe 4
      res.map(_.rows.count(row => row.actions.toString.contains("-"))).get mustBe 0
    }

    "return a JsError if section is missing an answer" in {
      val invalidJson = Json.parse(
        """
          |{ "sections": [
          |            {
          |              "title": "section_1",
          |              "data": [
          |                {"questionId": "foo", "question": "Question 1", "answer": "FOO", "answerValue": true},
          |                {"questionId": "bar", "question": "Question 1", "answerValue": true}
          |                ]
          |             }
          |           ]
          |  }
        """.stripMargin
      )

      val res = Json.fromJson[SummaryList](invalidJson)(
        EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, changeText)
      )

      res.isError mustBe true
      res.asEither.left.get.head._1.toJsonString mustBe "obj.sections[0].data[1].answer"
    }

    "return a JsError if section is missing a question" in {
      val invalidJson = Json.parse(
        """
          |{ "sections": [
          |            {
          |              "title": "section_1",
          |              "data": [
          |                {"questionId": "mandatoryRegistration", "answer": "FOO", "answerValue": true},
          |                {"questionId": "mandatoryRegistration", "question": "Question 1", "answerValue": true}
          |                ]
          |             }
          |           ]
          |  }
        """.stripMargin
      )

      val res = Json.fromJson[SummaryList](invalidJson)(
        EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, changeText)
      )

      res.isError mustBe true
      res.asEither.left.get.head._1.toJsonString mustBe "obj.sections[0].data[0].question"
    }

    "return a JsError if section doesn't have data block" in {
      val invalidJson = Json.parse(
        """
          |{ "sections": [
          |            {
          |             "title" : "foo"
          |             }
          |           ]
          |  }
                                   """.stripMargin
      )

      val res = Json.fromJson[SummaryList](invalidJson)(
        EligibilityJsonParser.eligibilitySummaryListReads(eligibilityCall, changeText)
      )

      res.isError mustBe true
      res.asEither.left.get.head._1.toJsonString mustBe "obj.sections[0].data"
    }
  }
}