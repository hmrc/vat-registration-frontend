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

package viewmodels

import config.FrontendAppConfig
import models.view.SummaryListRowUtils.optSummaryListRowString
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList

class EligibilitySummaryBuilderSpec extends VatRegSpec {
  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    object Builder extends EligibilitySummaryBuilder(govukSummaryList)
  }

  "eligibilityCall" must {
    "return a full url" in new Setup {
      val res: String = Builder.eligibilityCall("page1OfEligibility")
      res mustBe "http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=page1OfEligibility"
    }
  }

  "getEligibilitySummary" must {
    "return a Summary when valid json is returned from vatregservice" in new Setup {
      Builder.build(fullEligibilityDataJson, testRegId) mustBe HtmlFormat.fill(
        List(
          govukSummaryList(SummaryList(List(
            optSummaryListRowString("Question 1", Some("FOO"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=mandatoryRegistration")),
            optSummaryListRowString("Question 2", Some("BAR"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=voluntaryRegistration")),
            optSummaryListRowString("Question 3", Some("wizz"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=thresholdPreviousThirtyDays")),
            optSummaryListRowString("Question 4", Some("woosh"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=thresholdInTwelveMonths")),
            optSummaryListRowString("Question 5", Some("bang"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=applicantUKNino")),
            optSummaryListRowString("Question 6", Some("BUZZ"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=turnoverEstimate")),
            optSummaryListRowString("Question 7", Some("cablam"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=completionCapacity")),
            optSummaryListRowString("Question 8", Some("weez"), Some("http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=completionCapacityFillingInFor")),
          ).flatten))
        )
      )
    }

    "return an exception when invalid json returned from vatregservice" in new Setup {
      intercept[Exception](Builder.build(Json.obj("invalid" -> "json"), testRegId))
    }
  }
}
