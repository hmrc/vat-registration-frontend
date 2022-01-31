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

package views.returns

import forms.ReturnFrequencyForm
import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.returns.return_frequency_view

class ReturnFrequencyViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[return_frequency_view]
  implicit val doc = Jsoup.parse(view(form = ReturnFrequencyForm.form, showAAS = true, showMonthly = false).body)

  object ExpectedContent {
    val subheading = "VAT registration"
    val heading = "When will the business do its VAT Returns?"
    val title = "When will the business do its VAT Returns?"
    val para = "Usually, VAT-registered businesses submit their VAT returns and payments to HM Revenue and Customs 4 times a year."
    val detailsSummary = "About the Annual Accounting Scheme"
    val detailsPara1 = "businesses on the scheme:"
    val detailsPara1Bullet1 = "submit one VAT Return a year, rather than quarterly or monthly returns"
    val detailsPara1Bullet2 = "make monthly or quarterly payments, based on an HMRC estimate of their end-of-year VAT bill"
    val detailsPara2 = "It may not suit businesses that:"
    val detailsPara2Bullet1 = "want to keep up to date with the exact amount of VAT they owe or need to reclaim"
    val detailsPara2Bullet2 = "regularly reclaim more VAT than they charge, because they will only get one VAT refund a year"
    val findOutMoreLinkText = "Find out more about the Annual Accounting Scheme (opens in new tab)"
    val label = "Select yes if you expect the business to regularly claim VAT refunds from HMRC"
    val continue = "Save and continue"
    val quarterly = "Quarterly"
    val annually = "The business would like to join the Annual Accounting Scheme"
  }

  "The return frequency page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct subheading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.subheading)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a progressive disclosure" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.detailsSummary,
        ExpectedContent.detailsPara1 + " " +
        ExpectedContent.detailsPara1Bullet1 + " " +
        ExpectedContent.detailsPara1Bullet2 + " " +
        ExpectedContent.detailsPara2 + " " +
        ExpectedContent.detailsPara2Bullet1 + " " +
        ExpectedContent.detailsPara2Bullet2 + " " +
        ExpectedContent.findOutMoreLinkText))
    }

    "have frequency radio options" in new ViewSetup {
      doc.radio("quarterly") mustBe Some(ExpectedContent.quarterly)
      doc.radio("annual") mustBe Some(ExpectedContent.annually)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
