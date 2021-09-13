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

import forms.InternationalAddressForm
import org.jsoup.Jsoup
import play.api.mvc.Call
import views.html.CaptureInternationalAddress

class CaptureInternationalAddressViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[CaptureInternationalAddress]
  val form = app.injector.instanceOf[InternationalAddressForm].form()

  object ExpectedMessages {
    val title = "Enter your home address"
    val h1 = "Enter your home address"
    val line1 = "Address line 1"
    val line2 = "Address line 2 (optional)"
    val line3 = "Address line 3 (optional)"
    val line4 = "Address line 4 (optional)"
    val line5 = "Address line 5 (optional)"
    val postcode = "Postcode (optional)"
    val country = "Country"
    val continue = "Continue"
  }

  implicit val doc = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.home.heading").body)

  "the Capture International Address page" should {
    "have the correct page title" in new ViewSetup {
      doc.title() must include(ExpectedMessages.title)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.h1)
    }
    "have a field for line 1" in new ViewSetup {
      doc.textBox("line1") mustBe Some(ExpectedMessages.line1)
    }
    "have a field for line 2" in new ViewSetup {
      doc.textBox("line2") mustBe Some(ExpectedMessages.line2)
    }
    "have a field for line 3" in new ViewSetup {
      doc.textBox("line3") mustBe Some(ExpectedMessages.line3)
    }
    "have a field for line 4" in new ViewSetup {
      doc.textBox("line4") mustBe Some(ExpectedMessages.line4)
    }
    "have a field for line 5" in new ViewSetup {
      doc.textBox("line5") mustBe Some(ExpectedMessages.line5)
    }
    "have a field for postcode" in new ViewSetup {
      doc.textBox("postcode") mustBe Some(ExpectedMessages.postcode)
    }
    "have a field for country" in new ViewSetup {
      doc.select("label[for=country]").toList.headOption.map(_.text) mustBe Some(ExpectedMessages.country)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.continue)
    }
  }

}
