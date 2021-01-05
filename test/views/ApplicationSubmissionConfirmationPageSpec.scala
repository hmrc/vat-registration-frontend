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

import models.Stagger
import org.jsoup.Jsoup
import views.html.pages.application_submission_confirmation

class ApplicationSubmissionConfirmationPageSpec extends VatRegViewSpec {

  val title = "Your application has been submitted - Register for VAT - GOV.UK"
  val heading = "Your application has been submitted"
  val heading2 = "What happens next"
  val heading3 = "More information"
  val paragraph = "We have received your application and will write to you with a decision. This usually takes about 20 days."
  val paragraph2 = "Find out more about:"
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
  val hiddenText = "feb"
  val viewInstance: application_submission_confirmation = app.injector.instanceOf[application_submission_confirmation]

  "Application submission confirmation page" should {
    lazy val view = viewInstance(Some(Stagger.feb))
    lazy val doc = Jsoup.parse(view.body)

    "have the correct title" in {
      doc.title must include(title)
    }

    "have the correct headings" in {
      doc.selectFirst(Selectors.h1).text mustBe heading
      doc.select(Selectors.h1).get(1).text mustBe heading2
      doc.select(Selectors.h1).get(2).text mustBe heading3
    }

    "have the correct paragraphs" in {
      doc.select(Selectors.p(1)).text mustBe paragraph
      doc.select(Selectors.p(2)).text mustBe paragraph2
    }

    "have the correct list" in {
      doc.select(Selectors.orderedList(1)).text mustBe listItem1
      doc.select(Selectors.orderedList(2)).text mustBe listItem2
    }

    "have the correct Link texts" in {
      doc.select(Selectors.a(1)).get(0).text mustBe linkText1
      doc.select(Selectors.a(1)).get(1).text mustBe linkText2
      doc.select(Selectors.a(1)).get(2).text mustBe linkText3
    }

    "have the correct Link urls" in {
      doc.select(Selectors.a(1)).get(0).attr("href") mustBe url1
      doc.select(Selectors.a(1)).get(1).attr("href") mustBe url2
      doc.select(Selectors.a(1)).get(2).attr("href") mustBe url3
    }

    "have the correct inset text" in {
      doc.select(Selectors.indent).text mustBe insetText
    }

    "have the correct continue button" in {
      doc.select(Selectors.button).text mustBe buttonText
    }

    "have the correct hidden text" in {
      doc.select(Selectors.hidden).text mustBe hiddenText
    }

  }

}