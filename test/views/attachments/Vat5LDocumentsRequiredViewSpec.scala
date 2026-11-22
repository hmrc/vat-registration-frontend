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

import config.FrontendAppConfig
import models.api._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.Vat5LDocumentsRequired

class Vat5LDocumentsRequiredViewSpec extends VatRegViewSpec {

  val view: Vat5LDocumentsRequired = app.injector.instanceOf[Vat5LDocumentsRequired]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "We need documents from you to process your VAT application"
    val title = s"$heading - Register for VAT - GOV.UK"

    val para1 =
      "Based on your answers, we still need forms from you. Select a form to get more information about each one, and to download them so they can be completed and printed (links open in a new tab)"

    val para2 = "You need to send us:"

    val vat5L =
      "a completed VAT5L form to tell us about your land and properties supplies (you can also include any supporting documents)"

    val proofOfIdentity = "3 documents to confirm your identity"

    val landHeading = "If you want to charge VAT on land or buildings"

    val para3 = "You also need to send:"

    val vat1614A =
      "VAT1614A - to notify HMRC of your decision to opt to tax land and or buildings"

    val vat1614H =
      "VAT1614H - if you want to opt to tax land and or buildings and need HMRC permission"

    val identityHeading = "What identity documents can I provide?"

    val continue = "Continue"
  }

  "VAT5L documents required page" must {

    "have the correct heading and title" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
      doc.title mustBe ExpectedContent.title
    }

    "have the correct introductory paragraphs" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "display the required VAT5L and identity information" in new ViewSetup {
      val pageText: String = doc.text()

      pageText must include(ExpectedContent.vat5L)
      pageText must include(ExpectedContent.proofOfIdentity)
    }

    "display the VAT5L guidance link with the correct URL" in new ViewSetup {
      val vat5LGuidanceLink = doc.select("a:containsOwn(VAT5L form)")

      vat5LGuidanceLink.size() mustBe 1
      vat5LGuidanceLink.attr("href") mustBe appConfig.vat5LGuidanceLink
      vat5LGuidanceLink.attr("target") mustBe "_blank"
    }

    "display the land and buildings section" in new ViewSetup {
      val pageText: String = doc.text()

      doc.select("h2").text() must include(ExpectedContent.landHeading)

      pageText must include(ExpectedContent.para3)
      pageText must include(ExpectedContent.vat1614A)
      pageText must include(ExpectedContent.vat1614H)
    }

    "display the VAT1614A and VAT1614H guidance links with the correct URLs" in new ViewSetup {
      val vat1614ALink = doc.select("a:containsOwn(VAT1614A)")
      val vat1614HLink = doc.select("a:containsOwn(VAT1614H)")

      vat1614ALink.size() mustBe 1
      vat1614ALink.attr("href") mustBe appConfig.vat1614ALink
      vat1614ALink.attr("target") mustBe "_blank"

      vat1614HLink.size() mustBe 1
      vat1614HLink.attr("href") mustBe appConfig.vat1614HLink
      vat1614HLink.attr("target") mustBe "_blank"
    }

    "display the identity documents details section" in new ViewSetup {
      doc.text() must include(ExpectedContent.identityHeading)
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }

    "not display the save progress button" in new ViewSetup {
      doc.text() must not include "Save and come back later"
    }
  }
}
