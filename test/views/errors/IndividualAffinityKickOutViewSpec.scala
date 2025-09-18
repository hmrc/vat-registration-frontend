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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.errors.IndividualAffinityKickOut

class IndividualAffinityKickOutViewSpec extends VatRegViewSpec {

  val view: IndividualAffinityKickOut = app.injector.instanceOf[IndividualAffinityKickOut]
  implicit val doc: Document = Jsoup.parse(view().body)

  object ExpectedContent {
    val title = "You need to use a business tax account for this service"
    val header = "You need to use a business tax account for this service"
    val text = "To register for VAT and manage your VAT online, you need to use a business tax account. The account you have signed into is for personal tax."
    val signInLinkText = "Sign into your organisation’s business tax account"
    val createLinkText = "Create a business tax account"
    val signInLink = "/register-for-vat/error/individual-affinity-signin"
    val createLink = "/register-for-vat/error/individual-affinity-redirect"
  }

  "Individual Affinity Kickout Page" must {

    "have the right title" in {
      doc.title() mustBe ExpectedContent.title
    }

    "have the right header" in {
      doc.select(Selectors.h1).text() mustBe ExpectedContent.header
    }

    "have the right text" in {
      doc.select(Selectors.p).text() mustBe ExpectedContent.text
    }

    "have the correct sign-in link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.signInLinkText, ExpectedContent.signInLink))
    }

    "have the correct create link text" in new ViewSetup {
      doc.link(2) mustBe Some(Link(ExpectedContent.createLinkText, ExpectedContent.createLink))
    }
  }
}
