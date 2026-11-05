/*
 * Copyright 2026 HM Revenue & Customs
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

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import play.api.data.Forms._
import views.VatRegViewSpec
import views.html.attachments.PostalConfirmation

class PostalConfirmationViewSpec extends VatRegViewSpec {

  lazy val view: PostalConfirmation =
    app.injector.instanceOf[PostalConfirmation]

  private val postalConfirmationForm: Form[Boolean] =
    Form(single("value" -> boolean))

  object ExpectedContent {
    val heading = "Posting your additional forms and documents"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "Uploading, instead of posting, your forms and documents will usually mean we can process your application quicker."
    val warning = "If you still want to post them, you must do this after you have submitted your VAT registration application. Once you have, we will generate the cover letter that must be sent with your forms and documents."
    val question = "Do you still want to post your additional forms and documents?"
    val yes = "Yes"
    val no = "No, I want to upload them"
    val continue = "Continue"
  }

  "The Postal Confirmation page" must {

    implicit val doc: Document =
      Jsoup.parse(view(postalConfirmationForm).body)

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedContent.heading)
    }

    "have the correct introductory paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct warning text" in new ViewSetup {
      doc.warningText(1) match {
        case Some(value) =>
          value must include(ExpectedContent.warning)
        case None =>
          fail("Warning text was not rendered")
      }
    }

    "have the correct question heading" in new ViewSetup {
      doc.headingLevel2(1) mustBe Some(ExpectedContent.question)
    }

    "display the Yes radio option" in new ViewSetup {
      doc.select("input[type=radio][value=true]").size mustBe 1

      doc.select("label[for=value]").text must include(ExpectedContent.yes)
    }

    "display the No radio option with the correct label" in new ViewSetup {
      doc.select("label").eachText().toArray
        .map(_.toString) must contain(ExpectedContent.no)
    }

    "display the continue button" in new ViewSetup {
      doc.select("button[type=submit]").text must include(ExpectedContent.continue)
    }

    "submit the form to the postal confirmation controller" in new ViewSetup {
      val form: Element = doc.select("form").first()

      form.attr("action") must include(
        controllers.attachments.routes.PostalConfirmationController.submit.url
      )
    }

    "disable autocomplete on the form" in new ViewSetup {
      doc.select("form").attr("autocomplete") mustBe "off"
    }
  }
}