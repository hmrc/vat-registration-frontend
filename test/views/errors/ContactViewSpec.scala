/*
 * Copyright 2023 HM Revenue & Customs
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
import views.VatRegViewSpec
import views.html.errors.ContactView

class ContactViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[ContactView]
  implicit val doc = Jsoup.parse(view().body)
  val oshLink = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/online-services-helpdesk"

  object ExpectedMessages {
    val h1 = "Sorry this service is unavailable"
    val link1 = "HM Revenue & Customs Online Service Helpdesk"
    val link2 = "Sign out"
    val p = s"Sorry the system is unavailable. Please try again later or contact the $link1."
  }

  "the contact view" must {
    "have the correct title" in new ViewSetup {
      doc.title() must include(ExpectedMessages.h1)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.h1)
    }
    "have the correct first paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.p)
    }
    "have the correct OSH link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedMessages.link1, oshLink))
    }
    "have the correct sign out link" in new ViewSetup {
      doc.link(2) mustBe Some(Link(ExpectedMessages.link2, controllers.callbacks.routes.SignInOutController.signOut.url))
    }
  }

}
