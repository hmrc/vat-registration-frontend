/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.Jsoup
import play.api.data.Form
import play.api.data.Forms.{boolean, single}
import views.html.annualAccountingScheme.annual_accounting_scheme

class AnnualAccountingSchemeViewSpec extends VatRegViewSpec {

  val form = Form(single("value" -> boolean))
  val view = app.injector.instanceOf[annual_accounting_scheme]
  implicit val doc = Jsoup.parse(view(form).body)

  object ExpectedContent {
    val heading = "Do you want to apply for the Annual Accounting Scheme?"
    val title = "Do you want to apply for the Annual Accounting Scheme?"
    val paragraph = "Accounting once a year is an option for companies with an estimated VAT-taxable turnover of £1.35 million or less."
    val detailsSummaryText = "About the Annual Accounting Scheme"
    val detailsContent = "Companies on the scheme:" + "submit one VAT Return a year, rather than quarterly or monthly returns" +
      "make monthly or quarterly payments, based on an HMRC estimate of their end-of-year VAT bill" + "It may not suit companies that:" +
      "want to keep up to date with the exact amount of VAT they owe or need to reclaim" + "regularly reclaim more VAT than they charge, because they will only get one VAT refund a year"
    val linkText = "Find out more about the Annual Accounting Scheme (opens in new tab)"
    val label = "Select yes if you want to apply for the Annual Accounting Scheme"
    val buttonText = "Save and continue"
    val yes = "Yes"
    val no = "No"
  }

  "Annual Accounting Scheme page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a progressive disclosure" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.detailsSummaryText, ExpectedContent.detailsContent))
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, "https://www.gov.uk/vat-annual-accounting-scheme"))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.buttonText)
    }
  }

}
