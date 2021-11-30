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
import org.jsoup.nodes.Document
import play.twirl.api.Html
import views.html.EmailCoverSheet

class EmailCoverSheetViewSpec extends VatRegViewSpec {

  val emailCoverSheetPage: EmailCoverSheet = app.injector.instanceOf[EmailCoverSheet]

  val testRef = "VRN12345689"

  lazy val view: Html = emailCoverSheetPage(ackRef = testRef)
  implicit val doc: Document = Jsoup.parse(view.body)

  val heading = "How to email documents to HMRC"
  val title = s"$heading - Register for VAT - GOV.UK"
  val para1 = "The subject line of the email must include your Register for VAT reference number. This will enable us to match your online application to your supporting documents."
  val panel1 = s"Register for VAT reference number: $testRef"
  val heading2 = "What you must attach to the email"
  val para2 = "Include a copy of one piece of evidence that includes a government issued photo. This could be a:"

  val heading3 = "And"
  val para3 = "Also include two additional pieces of evidence which can be copies of a:"

  val heading4 = "Email address"
  val para4 = "Send the supporting documents to:"

  val bullet1 = "passport"
  val bullet2 = "driving licence photocard"
  val bullet3 = "national identity card"
  val bullet4 = "mortgage statement"
  val bullet5 = "lease or rental agreement"
  val bullet6 = "work permit or visa"
  val bullet7 = "letter from the Department for Work and Pensions for confirming entitlement to benefits"
  val bullet8 = "utility bill"
  val bullet9 = "birth certificate"

  val panel2 = "vrs.newregistrations@hmrc.gov.uk"

  val print = "Print this page"

  "The Email Cover Sheet page" must {
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).get(0).text mustBe panel1
    }

    "have the correct heading2" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(heading2)
    }
    "have the correct heading3" in new ViewSetup {
      doc.headingLevel2(2) mustBe Some(heading3)
    }
    "have the correct heading4" in new ViewSetup {
      doc.headingLevel2(3) mustBe Some(heading4)
    }

    "have the correct page title" in new ViewSetup {
      doc.title mustBe title
    }

    "have the correct paragraph1 text" in new ViewSetup {
      doc.para(1) mustBe Some(para1)
    }

    "have the correct paragraph2 text" in new ViewSetup {
      doc.para(2) mustBe Some(para2)
    }

    "have the correct paragraph3 text" in new ViewSetup {
      doc.para(3) mustBe Some(para3)
    }

    "have the correct paragraph4 text" in new ViewSetup {
      doc.para(4) mustBe Some(para4)
    }

    "have the correct first bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        bullet1,
        bullet2,
        bullet3
      )
    }

    "have the correct second bullet list" in new ViewSetup {
      doc.unorderedList(2) mustBe List(
        bullet4,
        bullet5,
        bullet6,
        bullet7,
        bullet8,
        bullet9
      )
    }

    "have the correct panel text two" in new ViewSetup {
      doc.select(Selectors.indent).get(1).text mustBe panel2
    }
  }
}
