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

package views

import forms.InternationalAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import views.html.CaptureInternationalAddress

class CaptureInternationalAddressViewSpec extends VatRegViewSpec {

  val testTransactorName = Some("John")

  val view = app.injector.instanceOf[CaptureInternationalAddress]
  val form = app.injector.instanceOf[InternationalAddressForm].form()

  object ExpectedMessages {
    val title = "Enter your home address"
    val heading = "Enter your home address"
    val thirdPartyHeading = "Enter " + testTransactorName.get + "’s home address"
    val line1 = "Address line 1"
    val line2 = "Address line 2"
    val line3 = "Address line 3 (optional)"
    val line4 = "Address line 4 (optional)"
    val line5 = "Address line 5 (optional)"
    val postcode = "Postcode"
    val postcodeOptional = "Postcode (optional)"
    val ppobHeading = "Enter the primary place of business address"
    val postcodeHint = "You only need to enter a postcode if the address is in the United Kingdom, Guernsey, Jersey or the Isle of Man"
    val ppobHint = "You only need to enter a postcode if the address is in Guernsey, Jersey or the Isle of Man"
    val ppobPara = "This is for non-UK addresses, as you told us your business has no fixed establishments in the UK or Isle of Man."
    val country = "Country"
    val saveAndContinue = "Save and continue"
  }

  val transactorDoc = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.home.3pt.heading", name = testTransactorName).body)
  implicit val doc = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.home.heading", name = None).body)

  "the Capture International Address page" should {
    "have the correct page title" in new ViewSetup {
      doc.title() must include(ExpectedMessages.title)
    }
    "have the correct heading when the user is not transactor" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct heading when the user is transactor" in {
      transactorDoc.select(Selectors.h1).text mustBe ExpectedMessages.thirdPartyHeading
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
    "have a field for country" in new ViewSetup {
      doc.select("label[for=country]").toList.headOption.map(_.text) mustBe Some(ExpectedMessages.country)
    }
    "have a field for postcode" in new ViewSetup {
      doc.textBox("postcode") mustBe Some(ExpectedMessages.postcode)
    }
    "have the correct hint text" in new ViewSetup {
      doc.hintText mustBe Some(ExpectedMessages.postcodeHint)
    }
    "have the correct heading and hint text when on the ppob page" in new ViewSetup {
      val ppobDoc = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.ppob.heading", name = testTransactorName, isPpob = true).body)
      ppobDoc.heading mustBe Some(ExpectedMessages.ppobHeading)
      ppobDoc.hintText mustBe Some(ExpectedMessages.ppobHint)
    }
    "have the correct paragraph on the ppob page" in new ViewSetup {
      val ppobDoc: Document = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.ppob.heading", name = testTransactorName, isPpob = true).body)
      ppobDoc.para(1) mustBe Some(ExpectedMessages.ppobPara)
    }
    "have a field for postcode (optional) on the ppob page" in new ViewSetup {
      val ppobDoc: Document = Jsoup.parse(view(form, Seq(), submitAction = Call("GET", "/"), headingKey = "internationalAddress.ppob.heading", name = testTransactorName, isPpob = true).body)
      ppobDoc.textBox("postcode") mustBe Some(ExpectedMessages.postcodeOptional)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.saveAndContinue)
    }
  }

}
