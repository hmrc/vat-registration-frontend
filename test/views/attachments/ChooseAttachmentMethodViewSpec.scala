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

package views.attachments

import featuretoggle.FeatureToggleSupport
import forms.AttachmentMethodForm
import models.api.Upload
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import views.VatRegViewSpec
import views.html.attachments.ChooseAttachmentMethod

class ChooseAttachmentMethodViewSpec extends VatRegViewSpec with FeatureToggleSupport {

  object ExpectedMessages {
    val heading = "How will you send the additional documents?"
    val upload = "Upload the documents using this service"
    val uploadHint = "Uploaded documents will be saved for 6 hours. After this time, the application will remain saved but documents must be uploaded again."
    val post = "Post copies to HMRC"
    val button = "Continue"
  }

  "The Choose Attachment Method page" must {

    val view = app.injector.instanceOf[ChooseAttachmentMethod]
    val form = app.injector.instanceOf[AttachmentMethodForm]
    implicit val doc: Document = Jsoup.parse(view(form()).body)

    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }

    "have the correct radio options" in new ViewSetup {
      doc.radio("2") mustBe Some(ExpectedMessages.upload)
      doc.radio("3") mustBe Some(ExpectedMessages.post)
      doc.radio("email") mustBe None
    }

    "have the upload hint" in new ViewSetup {
      doc.body.text must include(ExpectedMessages.uploadHint)
    }

    "show the upload hint when Upload is preselected" in new ViewSetup {
      val uploadDoc: Document = Jsoup.parse(view(form.apply().bind(Map("value" -> "2"))).body)
      val conditionalBlock: Element = uploadDoc.select("div.govuk-radios__conditional").first()

      conditionalBlock.text() must include(ExpectedMessages.uploadHint)
      conditionalBlock.hasClass("govuk-radios__conditional--hidden") mustBe false
    }

    "not show the upload hint when Post is preselected" in new ViewSetup {
      val postDoc: Document = Jsoup.parse(view(form.apply().bind(Map("value" -> "3"))).body)
      val conditionalBlock: Element = postDoc.select("div.govuk-radios__conditional").first()

      conditionalBlock.hasClass("govuk-radios__conditional--hidden") mustBe true
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.button)
    }
  }

}
