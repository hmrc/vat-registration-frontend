/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.genericForms.YesOrNoFormFactory
import org.jsoup.Jsoup
import views.html.frs_join

class JoinFrsViewSpec extends VatRegViewSpec {

  val form = YesOrNoFormFactory.form()("frs.join")
  val view = app.injector.instanceOf[frs_join]

  object ExpectedContent {
    val heading = "Do you want to register the business for the Flat Rate Scheme?"
    val title = "Do you want to register the business for the Flat Rate Scheme?"

    object List1 {
      val summary = "Businesses on the Flat Rate Scheme:"
      val bullet1 = "pay a fixed rate of VAT to HMRC, based on their business type"
      val bullet2 = "keep the difference between what they charge customers and pay to HMRC"
      val bullet3 = "can’t reclaim VAT on their purchases (except for certain capital assets over £2,000)"
      val bullet4 = "usually have simpler VAT records than businesses on the standard rate scheme"
      val after = "They can also take an extra 1% off their fixed rate in the first year of VAT registration."
    }

    object Details {
      val summary = "Show me an example"
      val content = "A dry cleaners on the Flat rate Scheme does £20,000 worth of work in a quarter. It charges 20% VAT " +
        "and its sales, including £4,000 for VAT, total £24,000. " +
        "This type of business pays a VAT flat rate of 12% of total sales, which is £2,880. This is less than the £4,000 " +
        "it charged for VAT and it can keep the £1,120 difference."
    }

    val heading2 = "Before you decide"

    object List2 {
      val summary = "You’ll need to have an idea how much the business will:"
      val bullet1 = "spend on goods to run the business over the next 3 months"
      val bullet2 = "earn in sales, including VAT, over the next 3 months"
    }

    val para2 = "If you can’t decide right now, answer ’no’. You can register the business for the Flat Rate Scheme at a later date."
    val link = "VAT Flat Rate Scheme"
    val para3 = s"Find out more about $link."
    val label = "Tell us if you want to register the business for the Flat Rate Scheme"
    val yes = "Yes"
    val no = "No"
    val continue = "Continue"
  }

  implicit val doc = Jsoup.parse(view(form).body)

  "The Join FRS page" must {
    "have a back link" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have a list detailing who can join FRS" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.List1.summary)
      doc.unorderedList(1) mustBe List(
        ExpectedContent.List1.bullet1,
        ExpectedContent.List1.bullet2,
        ExpectedContent.List1.bullet3,
        ExpectedContent.List1.bullet4
      )
      doc.para(2) mustBe Some(ExpectedContent.List1.after)
    }

    "have a progressive disclosure" in new ViewSetup {
      doc.details mustBe Some(Details(ExpectedContent.Details.summary, ExpectedContent.Details.content))
    }

    "have a second heading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.heading2)
    }

    "have a second list" in new ViewSetup {
      doc.para(3) mustBe Some(ExpectedContent.List2.summary)
      doc.unorderedList(2) mustBe List(
        ExpectedContent.List2.bullet1,
        ExpectedContent.List2.bullet2
      )
    }

    "have a final paragraph with a help link" in new ViewSetup {
      doc.para(4) mustBe Some(ExpectedContent.para2)
      doc.para(5) mustBe Some(ExpectedContent.para3)
      doc.link(1) mustBe Some(Link(ExpectedContent.link, "https://www.gov.uk/vat-flat-rate-scheme"))
    }

    "have yes/no radio options" in new ViewSetup {
      doc.radio("true") mustBe Some(ExpectedContent.yes)
      doc.radio("false") mustBe Some(ExpectedContent.no)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
