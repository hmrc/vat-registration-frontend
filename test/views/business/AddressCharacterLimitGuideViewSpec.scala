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

package views.business

import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.business.AddressCharacterLimitGuideView

class AddressCharacterLimitGuideViewSpec extends VatRegViewSpec {

  val view: AddressCharacterLimitGuideView = app.injector.instanceOf[AddressCharacterLimitGuideView]

  object ExpectedContent {
    val title = "Important information about your primary place of business - Register for VAT - GOV.UK"
    val heading = "Important information about your primary place of business"
    val usuallyPlace = "It is usually the place where:"
    val bullet1 = "orders are received and dealt with"
    val bullet2 = "the day to day running of the business takes place"
    val awareHeading = "You should also be aware of the following when adding the address:"
    val characterLimit = "There is a 35-character limit for each line of address"
    val addressLookup = "On the next page, the ‘address look-up’ will automatically shorten any address line that exceeds 35 characters."
    val systemLimitations = "This is due to system limitations and will not affect your registration or HMRC's ability to write to you."
    val warningText1 = "If you still wish to manually edit the address back to the address in full you should use another address line to split up the part of the address that exceeds 35 characters."
    val warningText2 = "If any single address line exceeds 35 characters your registration will not be accepted when you submit it and you will have to start again."
    val invalidAddresses = "Invalid addresses"
    val doNotUse = "Do not use:"
    val invalidBullet1 = "the address of a third-party accountant or tax agent"
    val invalidBullet2 = "a PO box address"
    val invalidBullet3 = "a ‘care of’ or other third-party address, including virtual or serviced offices or mail forwarding and mailbox services"
    val englishCharacters = "Use English characters"
    val onlyEnglish = "Only enter English characters, even if the address is not in the UK."
    val insetText = "If you are registered for VAT, your primary place of business will be visible on the"
    val linkText = "Check a UK VAT Number service (opens in new tab)"
    val continueButton = "Continue"
  }

  "AddressCharacterLimitGuideView" must {
    implicit val doc = Jsoup.parse(view().body)

    "have the correct title" in {
      doc.title must include(ExpectedContent.title)
    }

    "have the correct heading" in {
      doc.select("h1").text mustBe ExpectedContent.heading
    }

    "have the correct bullet points for usual place" in {
      val bullets = doc.select("ul.govuk-list--bullet").first.select("li")
      bullets.get(0).text mustBe ExpectedContent.bullet1
      bullets.get(1).text mustBe ExpectedContent.bullet2
    }

    "have the correct subheading for character limit" in {
      doc.select("h2.govuk-heading-s").first.text mustBe ExpectedContent.characterLimit
    }

    "have the correct warning text" in {
      val warningText = doc.select(".govuk-warning-text__text").text
      warningText must include(ExpectedContent.warningText1)
      warningText must include(ExpectedContent.warningText2)
    }

    "have the correct subheading for invalid addresses" in {
      doc.select("h2.govuk-heading-s").get(1).text mustBe ExpectedContent.invalidAddresses
    }

    "have the correct bullet points for invalid addresses" in {
      val bullets = doc.select("ul.govuk-list--bullet").get(1).select("li")
      bullets.get(0).text mustBe ExpectedContent.invalidBullet1
      bullets.get(1).text mustBe ExpectedContent.invalidBullet2
      bullets.get(2).text mustBe ExpectedContent.invalidBullet3
    }

    "have the correct subheading for English characters" in {
      doc.select("h2.govuk-heading-s").last.text mustBe ExpectedContent.englishCharacters
    }

    "have the correct inset text" in {
      doc.select(".govuk-inset-text").text must include(ExpectedContent.insetText)
    }

    "have the correct link text" in {
      doc.select(".govuk-inset-text a").text mustBe ExpectedContent.linkText
    }

    "have a continue button" in {
      doc.select("button").text mustBe ExpectedContent.continueButton
    }
  }
}
