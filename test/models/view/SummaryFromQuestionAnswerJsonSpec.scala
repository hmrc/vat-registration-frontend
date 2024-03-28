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

package models.view

import config.FrontendAppConfig
import fixtures.VatRegistrationFixture
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class SummaryFromQuestionAnswerJsonSpec(implicit appConfig:FrontendAppConfig) extends VatRegSpec with VatRegistrationFixture {

  def eligibilityCall(uri: String): String = s"http://vatRegEFEUrl/question?pageId=$uri"

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

  "summaryReads" should {
    "return a summary with from Full Json" in {
      val res = Json.fromJson[SummaryList](fullEligibilityDataJson)(
        EligibilityJsonParser.reads(eligibilityCall)
      )

      res.map(_.rows.length).get mustBe 9
    }

    "eligibility cya threshold answers are displayed as one entry with the date when answer is true" in {
      val eligibilityJsonWithQuestionIdDashes = Json.obj(
        "registrationReason" -> "selling-goods-and-services",
        "thresholdPreviousThirtyDays" -> Json.obj(
          "value" -> true,
          "optionalData" -> "2021-12-01"
        )
      )

      val res = Json.fromJson[SummaryList](eligibilityJsonWithQuestionIdDashes)(
        EligibilityJsonParser.reads(eligibilityCall)
      )

      res.map(_.rows.length).get mustBe 2
      res.map(_.rows.map(_.value.content.asHtml.toString())).get mustBe Seq("It’s selling goods or services and needs or wants to charge VAT to customers", "Yes - on 1 December 2021")
    }

    "eligibility cya threshold sections should not be flattened when the use chooses no and there is no optional data available" in {
      val eligibilityJsonWithQuestionIdDashes = Json.obj(
        "registrationReason" -> "selling-goods-and-services",
        "thresholdPreviousThirtyDays" -> Json.obj(
          "value" -> false
        )
      )

      val res = Json.fromJson[SummaryList](eligibilityJsonWithQuestionIdDashes)(
        EligibilityJsonParser.reads(eligibilityCall)
      )

      res.map(_.rows.length).get mustBe 2
      res.map(_.rows.map(_.value.content.asHtml.toString())).get mustBe Seq("It’s selling goods or services and needs or wants to charge VAT to customers", "No")
    }

    "eligibility cya business entity section should expand to include partnership type when user selects partnership entity type" in {
      val eligibilityJsonWithQuestionIdDashes = Json.obj(
        "registrationReason" -> "selling-goods-and-services",
        "businessEntity" -> "62"
      )

      val res = Json.fromJson[SummaryList](eligibilityJsonWithQuestionIdDashes)(
        EligibilityJsonParser.reads(eligibilityCall)
      )

      res.map(_.rows.length).get mustBe 3
      res.map(_.rows.map(_.value.content.asHtml.toString())).get mustBe Seq("Partnership", "Limited partnership", "It’s selling goods or services and needs or wants to charge VAT to customers")
    }

    "eligibility cya business entity section should not expand for other business entity types" in {
      val eligibilityJsonWithQuestionIdDashes = Json.obj(
        "registrationReason" -> "selling-goods-and-services",
        "businessEntity" -> "50"
      )

      val res = Json.fromJson[SummaryList](eligibilityJsonWithQuestionIdDashes)(
        EligibilityJsonParser.reads(eligibilityCall)
      )

      res.map(_.rows.length).get mustBe 2
      res.map(_.rows.map(_.value.content.asHtml.toString())).get mustBe Seq("UK company (includes Limited and Unlimited companies)", "It’s selling goods or services and needs or wants to charge VAT to customers")
    }
  }
}