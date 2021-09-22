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
import views.html.pages.application_submission_confirmation

class ApplicationSubmissionConfirmationViewSpec extends VatRegViewSpec {

  object ExpectedContent {
    val title = "Your application has been submitted - Register for VAT - GOV.UK"
    val heading = "Your application has been submitted"
    val heading2 = "What happens next"
    val heading3 = "More information"
    val paragraph = "We have received your application and will write to you with a decision. This usually takes about 20 days."
    val paragraph2 = "Find out more about:"
    val listPostItem = "Post copies of the supporting documents and the cover letter to HMRC so we can process your application. You can print the cover letter here (opens in new tab)"
    val listItem1 = "Wait for your letter which will confirm whether you have been registered for VAT or granted an exemption or exception."
    val listItem2 = "Wait until your registration is confirmed before getting software, if you are going to follow the rules for Making Tax Digital for VAT."
    val linkText1 = "VAT (opens in new tab)"
    val linkText2 = "Making Tax Digital for VAT (opens in new tab)"
    val linkText3 = "Software for Making Tax Digital for VAT (opens in new tab)"
    val url1 = "https://www.gov.uk/topic/business-tax/vat"
    val url2 = "https://www.gov.uk/government/collections/making-tax-digital-for-vat"
    val url3 = "https://www.gov.uk/guidance/find-software-thats-compatible-with-making-tax-digital-for-vat"
    val insetText = "The company cannot charge customers for VAT until it has its VAT number."
    val buttonText = "Finish"
  }

  val viewInstance: application_submission_confirmation = app.injector.instanceOf[application_submission_confirmation]

  "Application submission confirmation page" should {
    lazy val view = viewInstance(false)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct headings" in {
      doc.selectFirst(Selectors.h1).text mustBe ExpectedContent.heading
      doc.select(Selectors.h1).get(1).text mustBe ExpectedContent.heading2
      doc.select(Selectors.h1).get(2).text mustBe ExpectedContent.heading3
    }

    "have the correct paragraphs" in {
      doc.select(Selectors.p(1)).text mustBe ExpectedContent.paragraph
      doc.select(Selectors.p(2)).text mustBe ExpectedContent.paragraph2
    }

    "have the correct list" in {
      doc.select(Selectors.orderedList(1)).text mustBe ExpectedContent.listItem1
      doc.select(Selectors.orderedList(2)).text mustBe ExpectedContent.listItem2
    }

    "have the correct Link texts" in {
      doc.select(Selectors.a(1)).get(0).text mustBe ExpectedContent.linkText1
      doc.select(Selectors.a(1)).get(1).text mustBe ExpectedContent.linkText2
      doc.select(Selectors.a(1)).get(2).text mustBe ExpectedContent.linkText3
    }

    "have the correct Link urls" in {
      doc.select(Selectors.a(1)).get(0).attr("href") mustBe ExpectedContent.url1
      doc.select(Selectors.a(1)).get(1).attr("href") mustBe ExpectedContent.url2
      doc.select(Selectors.a(1)).get(2).attr("href") mustBe ExpectedContent.url3
    }

    "have the correct inset text" in {
      doc.select(Selectors.indent).text mustBe ExpectedContent.insetText
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text mustBe ExpectedContent.buttonText
    }
  }

  "Application submission confirmation page with IdentityEvidence" should {
    lazy val view = viewInstance(true)
    lazy val doc = Jsoup.parse(view.body)

    "have the correct list" in {
      doc.select(Selectors.orderedList(1)).text mustBe ExpectedContent.listPostItem
      doc.select(Selectors.orderedList(2)).text mustBe ExpectedContent.listItem1
      doc.select(Selectors.orderedList(3)).text mustBe ExpectedContent.listItem2
    }
  }
}
