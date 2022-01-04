/*
 * Copyright 2022 HM Revenue & Customs
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

package views.transactor

import forms.TransactorEmailAddressForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.transactor.TransactorCaptureEmailAddress

class TransactorCaptureEmailAddressViewSpec extends VatRegViewSpec {

  val title = "What is your email address?"
  val heading = "What is your email address?"
  val paragraph = "We will send you a confirmation code"
  val buttonText = "Save and continue"

  "Capture Email Address Page" should {
    val form = TransactorEmailAddressForm.form
    val view = app.injector.instanceOf[TransactorCaptureEmailAddress].apply(testCall, form)

    implicit val doc: Document = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(paragraph)
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }

  }

}
