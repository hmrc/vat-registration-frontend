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

package viewmodels

import config.FrontendAppConfig
import featureswitch.core.config.{FeatureSwitching, WelshLanguage}
import models.view.SummaryListRowUtils.optSummaryListRowString
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import testHelpers.VatRegSpec
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList
import uk.gov.hmrc.govukfrontend.views.html.components.GovukSummaryList

class EligibilitySummaryBuilderSpec extends VatRegSpec with FeatureSwitching {
  class Setup {
    val govukSummaryList = app.injector.instanceOf[GovukSummaryList]
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

    def url(questionId: String) = s"http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=$questionId&regId=$testRegId"

    object Builder extends EligibilitySummaryBuilder(govukSummaryList)
  }

  "eligibilityCall" must {
    "return a full url" in new Setup {
      val res: String = Builder.eligibilityCall(testRegId)("page1OfEligibility")
      res mustBe s"http://localhost:9894/check-if-you-can-register-for-vat/question?pageId=page1OfEligibility&regId=$testRegId"
    }
  }

  "getEligibilitySummary" must {
    "return a Summary when valid json is returned from vatregservice" in new Setup {
      Builder.build(fullEligibilityDataJson, testRegId) mustBe HtmlFormat.fill(
        List(
          govukSummaryList(SummaryList(List(
            optSummaryListRowString("Fixed UK establishment", Some("Yes"), Some(url("fixedEstablishment"))),
            optSummaryListRowString("Business type", Some("UK company (includes Limited and Unlimited companies)"), Some(url("businessEntity"))),
            optSummaryListRowString("Agricultural Flat Rate Scheme", Some("No"), Some(url("agriculturalFlatRateScheme"))),
            optSummaryListRowString("Activities over the next 12 months", Some("No"), Some(url("internationalActivities"))),
            optSummaryListRowString("Whose business to register", Some("Your own"), Some(url("registeringBusiness"))),
            optSummaryListRowString("VAT registration reason", Some("It’s selling goods or services and needs or wants to charge VAT to customers"), Some(url("registrationReason"))),
            optSummaryListRowString("Taxable turnover over £85,000 in 12 months", Some("Yes - on 16 July 2017"), Some(url("thresholdInTwelveMonths"))),
            optSummaryListRowString("Expect taxable turnover over £85,000 in 30 days past", Some("Yes - on 23 May 2017"), Some(url("thresholdPreviousThirtyDays"))),
            optSummaryListRowString("VAT registration exception", Some("No"), Some(url("vatRegistrationException")))
          ).flatten))
        )
      )
    }

    "return a Summary with welsh translated dates when language choice is welsh and FS enabled" in new Setup {
      enable(WelshLanguage)
      implicit val welshMessages = messagesApi.preferred(Seq(Lang("cy")))

      object WelshBuilder extends EligibilitySummaryBuilder(govukSummaryList)

      WelshBuilder.build(fullEligibilityDataJson, testRegId) mustBe HtmlFormat.fill(
        List(
          govukSummaryList(SummaryList(List(
            optSummaryListRowString("Sefydliad sefydlog yn y DU", Some("Iawn"), Some(url("fixedEstablishment"))),
            optSummaryListRowString("Math o fusnes", Some("Cwmni yn y DU (gan gynnwys cwmnïau Cyfyngedig ac Anghyfyngedig)"), Some(url("businessEntity"))),
            optSummaryListRowString("Cynllun Cyfradd Unffurf Amaethyddol", Some("Na"), Some(url("agriculturalFlatRateScheme"))),
            optSummaryListRowString("Gweithgareddau dros y 12 mis nesaf", Some("Na"), Some(url("internationalActivities"))),
            optSummaryListRowString("Busnes pwy sy’n cael ei gofrestru", Some("Eich un chi"), Some(url("registeringBusiness"))),
            optSummaryListRowString("Y rheswm dros gofrestru ar gyfer TAW", Some("Mae’n gwerthu nwyddau neu wasanaethau, ac mae angen neu eisiau codi TAW ar gwsmeriaid"), Some(url("registrationReason"))),
            optSummaryListRowString("Trosiant trethadwy dros £85,000 yn ystod 12 mis", Some("Iawn - ar 16 Gorffennaf 2017"), Some(url("thresholdInTwelveMonths"))),
            optSummaryListRowString("Yn disgwyl i drosiant trethadwy fod dros £85,000 yn y 30 diwrnod diwethaf", Some("Iawn - ar 23 Mai 2017"), Some(url("thresholdPreviousThirtyDays"))),
            optSummaryListRowString("Eithriad rhag cofrestru ar gyfer TAW", Some("Na"), Some(url("vatRegistrationException")))
          ).flatten))
        )
      )
      disable(WelshLanguage)
    }

    "return a Summary with english translated dates when language choice is welsh but FS not enabled" in new Setup {
      enable(WelshLanguage)
      Builder.build(fullEligibilityDataJson, testRegId) mustBe HtmlFormat.fill(
        List(
          govukSummaryList(SummaryList(List(
            optSummaryListRowString("Fixed UK establishment", Some("Yes"), Some(url("fixedEstablishment"))),
            optSummaryListRowString("Business type", Some("UK company (includes Limited and Unlimited companies)"), Some(url("businessEntity"))),
            optSummaryListRowString("Agricultural Flat Rate Scheme", Some("No"), Some(url("agriculturalFlatRateScheme"))),
            optSummaryListRowString("Activities over the next 12 months", Some("No"), Some(url("internationalActivities"))),
            optSummaryListRowString("Whose business to register", Some("Your own"), Some(url("registeringBusiness"))),
            optSummaryListRowString("VAT registration reason", Some("It’s selling goods or services and needs or wants to charge VAT to customers"), Some(url("registrationReason"))),
            optSummaryListRowString("Taxable turnover over £85,000 in 12 months", Some("Yes - on 16 July 2017"), Some(url("thresholdInTwelveMonths"))),
            optSummaryListRowString("Expect taxable turnover over £85,000 in 30 days past", Some("Yes - on 23 May 2017"), Some(url("thresholdPreviousThirtyDays"))),
            optSummaryListRowString("VAT registration exception", Some("No"), Some(url("vatRegistrationException")))
          ).flatten))
        )
      )
      disable(WelshLanguage)
    }

    "return an exception when invalid json returned from vatregservice" in new Setup {
      intercept[Exception](Builder.build(Json.obj("invalid" -> "json"), testRegId))
    }
  }
}
