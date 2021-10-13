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

import forms.AttachmentMethodForm
import org.jsoup.Jsoup
import views.html.ChooseAttachmentMethod

class ChooseAttachmentMethodViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[ChooseAttachmentMethod]
  val form = app.injector.instanceOf[AttachmentMethodForm]

  implicit val doc = Jsoup.parse(view(form()).body)

  object ExpectedMessages {
    val heading = "How would you like to send the additional documents?"
    val email = "Email copies to HMRC"
    val post = "Post copies to HMRC"
    val button = "Save and continue"
  }

  "The Choose Attachment Method page" must {
    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have the correct options" in new ViewSetup {
      doc.radio("3") mustBe Some(ExpectedMessages.post)
      doc.radio("email") mustBe Some(ExpectedMessages.email)
    }
    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.button)
    }
  }

}
