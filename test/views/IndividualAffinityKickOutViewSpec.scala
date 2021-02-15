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
import views.html.pages.error.individualAffinityKickOut

class IndividualAffinityKickOutViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[individualAffinityKickOut]
  implicit val doc = Jsoup.parse(view().body)

  val title = "Set up an organisation account"
  val header = "Set up an organisation account"
  val text = "To register for VAT and manage your VAT online, you need to have an organisation account. The account you currently have is a personal account."
  val link = "Sign out and create an organisation account"

  "Individual Affinity Kickout Page" must {

    "have the right title" in {
      doc.title() mustBe title
    }

    "have the right header" in {
      doc.select(Selectors.h1).text() mustBe header
    }

    "have the right text" in {
      doc.select(Selectors.p(1)).text() mustBe text
    }
    "have the correct link text" in new ViewSetup {
      doc.link(1) mustBe Some(Link(link, "/register-for-vat/error/individual-affinity-redirect"))
    }
  }
}
