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

package views.attachments

import featureswitch.core.config._
import forms.AttachmentMethodForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.attachments.ChooseAttachmentMethod

class ChooseAttachmentMethodViewSpec extends VatRegViewSpec with FeatureSwitching {

  object ExpectedMessages {
    val heading = "How would you like to send the additional documents?"
    val p = "You can only select one method."
    val upload = "Upload the documents using this service"
    val email = "Email copies to HMRC"
    val post = "Post copies to HMRC"
    val button = "Continue"
  }

  "The Choose Attachment Method page" must {

    enable(UploadDocuments)

    val view = app.injector.instanceOf[ChooseAttachmentMethod]
    val form = app.injector.instanceOf[AttachmentMethodForm]
    implicit val doc: Document = Jsoup.parse(view(form()).body)

    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.heading)
    }
    "have the correct page heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.p)
    }

    "have the correct radio options" in new ViewSetup {
      doc.radio("2") mustBe Some(ExpectedMessages.upload)
      doc.radio("3") mustBe Some(ExpectedMessages.post)
      doc.radio("email") mustBe None
    }

    "have a save and continue button" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedMessages.button)
    }

    "have the correct radio options when UploadDocuments FS is disabled" in new ViewSetup {
      disable(UploadDocuments)
      implicit val docWithUploadDisabled: Document = Jsoup.parse(view(form()).body)

      docWithUploadDisabled.radio("2") mustBe None
      docWithUploadDisabled.radio("3") mustBe Some(ExpectedMessages.post)
      docWithUploadDisabled.radio("email") mustBe Some(ExpectedMessages.email)
    }
  }

}
