/*
 * Copyright 2018 HM Revenue & Customs
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
import helpers.VatRegSpec
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.play.test.UnitSpec

class SummaryFromQuestionAnswerJsonSpec  extends UnitSpec with VatRegistrationFixture {

  "summaryReads" should {
    "return a summary with 2 sections from Full Json" in {
      val res = Json.fromJson[Summary](fullEligibilityDataJson)(SummaryFromQuestionAnswerJson.summaryReads).get
      res shouldBe fullSummaryModelFromFullEligiblityJson
    }

    "one section exists with questionId's with dashes in. all dashes and characters after the dash are removed for change links" in {
      val eligibilityJsonWithQuestionIdDashes = Json.parse("""
                                             |{ "sections": [
                                             |            {
                                             |              "title": "section_1",
                                             |              "data": [
                                             |                {"questionId": "mandatoryRegistration-foo", "question": "Question 1", "answer": "FOO", "answerValue": true},
                                             |                {"questionId": "voluntaryRegistration", "question": "Question 2", "answer": "BAR", "answerValue": false},
                                             |                {"questionId": "thresholdPreviousThirtyDays-", "question": "Question 3", "answer": "wizz", "answerValue": "2017-5-23"},
                                             |                {"questionId": "thresholdInTwelveMonths-foo-bar", "question": "Question 4", "answer": "woosh", "answerValue": "2017-7-16"}
                                             |              ]
                                             |            }]}""".stripMargin)
      val res = Json.fromJson[Summary](eligibilityJsonWithQuestionIdDashes)(SummaryFromQuestionAnswerJson.summaryReads).get
      res shouldBe Summary(section1 :: Nil)
    }
    "return a JsError if section is missing an answer" in {
      val invalidJson = Json.parse("""
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
        """.stripMargin)

      val res = Json.fromJson[Summary](invalidJson)(SummaryFromQuestionAnswerJson.summaryReads)
      res.isError shouldBe true
      res.asEither.left.get.head._1.toJsonString shouldBe "obj.sections[0][1].answer"
    }

    "return a JsError if section is missing a question" in {
      val invalidJson = Json.parse("""
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
        """.stripMargin)

      val res = Json.fromJson[Summary](invalidJson)(SummaryFromQuestionAnswerJson.summaryReads)
      res.isError shouldBe true
      res.asEither.left.get.head._1.toJsonString shouldBe "obj.sections[0][0].question"
    }
    "return a JsError if title is missing for a section" in {
      val invalidJson = Json.parse("""
                                     |{ "sections": [
                                     |            {
                                     |              "data": [
                                     |                {"questionId": "mandatoryRegistration", "question": "Question 1","answer": "bar", "answerValue": true}
                                     |                ]
                                     |             }
                                     |           ]
                                     |  }
                                   """.stripMargin)

      val res = Json.fromJson[Summary](invalidJson)(SummaryFromQuestionAnswerJson.summaryReads)
      res.isError shouldBe true
      res.asEither.left.get.head._1.toJsonString shouldBe "obj.sections[0]"
    }
    "return a JsError if section doesn't have data block" in {
      val invalidJson = Json.parse("""
                                     |{ "sections": [
                                     |            {
                                     |             "title" : "foo"
                                     |             }
                                     |           ]
                                     |  }
                                   """.stripMargin)

      val res = Json.fromJson[Summary](invalidJson)(SummaryFromQuestionAnswerJson.summaryReads)
      res.isError shouldBe true
      res.asEither.left.get.head._1.toJsonString shouldBe "obj.sections[0]"
    }
  }
}