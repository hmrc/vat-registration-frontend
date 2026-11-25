/*
 * Copyright 2026 HM Revenue & Customs
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

package views.attachments

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.MultipleDocumentsRequired

class MultipleDocumentsRequiredViewSpec extends VatRegViewSpec {

  val view: MultipleDocumentsRequired = app.injector.instanceOf[MultipleDocumentsRequired]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "We need documents from you to process your VAT application"
    val title = s"$heading - Register for VAT - GOV.UK"

    val para =
      "Based on your answers, we still need forms from you. Select a form to get more information about each one, and to download them so they can be completed and printed (links open in a new tab):"

    val vat2 = "VAT2 - partnership details"

    val vat5L =
      "VAT5L - land and property supplies (you can also include any supporting documents)"

    val vat5051 = "VAT 50/51 - VAT group registration"

    val vat1TR =
      "VAT1TR - a non-established taxable person (NETP) looking to appoint a tax representative"

    val proofOfIdentity = "3 documents to confirm your identity"

    val landHeading = "If you want to charge VAT on land or buildings"

    val landPara = "You also need to send:"

    val vat1614A =
      "VAT1614A - to notify HMRC of your decision to opt to tax land and or buildings"

    val vat1614H =
      "VAT1614H - if you want to opt to tax land and or buildings and need HMRC permission"

    val identityHeading = "What identity documents can I provide?"

    val continue = "Continue"
  }

  "Multiple documents required page" must {

    "have the correct heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
      doc.title mustBe ExpectedContent.title
    }

    "have the correct introductory text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "display all required forms and identity information" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.vat2)
      pageText must include(ExpectedContent.vat5L)
      pageText must include(ExpectedContent.vat5051)
      pageText must include(ExpectedContent.vat1TR)
      pageText must include(ExpectedContent.proofOfIdentity)
    }

    "display the land and buildings section" in new ViewSetup {
      val pageText: String = doc.text()

      doc.select("h2").text() must include(ExpectedContent.landHeading)

      pageText must include(ExpectedContent.landPara)
      pageText must include(ExpectedContent.vat1614A)
      pageText must include(ExpectedContent.vat1614H)
    }

    "display the identity documents details section" in new ViewSetup {
      doc.text() must include(ExpectedContent.identityHeading)
    }

    "display all required document links with the correct URLs" in new ViewSetup {
      val expectedLinks = Seq(
        "VAT2" -> appConfig.vat2Link,
        "VAT5L" -> appConfig.vat5LGuidanceLink,
        "VAT 50/51" -> appConfig.vat51Link,
        "VAT1TR" -> appConfig.vat1trLink,
        "VAT1614A" -> appConfig.vat1614ALink,
        "VAT1614H" -> appConfig.vat1614HLink
      )

      expectedLinks.foreach { case (linkText, expectedUrl) =>
        val link = doc.select(s"a:containsOwn($linkText)")

        link.size() mustBe 1
        link.attr("href") mustBe expectedUrl
        link.attr("target") mustBe "_blank"
      }
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }

    "not display the save progress button" in new ViewSetup {
      doc.text() must not include "Save and come back later"
    }
  }
}
