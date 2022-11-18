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

package views.applicant

import controllers.applicant.{routes => applicantRoutes}
import controllers.transactor.{routes => transactorRoutes}
import forms.EmailPasscodeForm
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.applicant.capture_email_passcode

class CaptureEmailPasscodeViewSpec extends VatRegViewSpec {

  val testEmail = "test@test.com"
  val heading = "Enter the code to confirm your email address"
  val headingNewPasscode = "Enter the new code"
  val title = s"$heading - Register for VAT - GOV.UK"
  val titleNewPasscode = s"$headingNewPasscode - Register for VAT - GOV.UK"
  val paragraph = s"We have sent a code to: $testEmail."
  val paragraphNewPasscode = s"We have sent a new code to: $testEmail."
  val insetText = "If you use a browser to access your email, you may need to open a new window or tab to see the code."
  val label = "Confirmation code"
  val hint = "For example, DNCLRK"
  val buttonText = "Save and continue"
  val detailsSummary = "I have not received the email"
  val linkText1 = "request a new code"
  val linkText2 = "provide another email address"
  val detailsContent = s"The email may take a few minutes to arrive. Its subject line is: ‘Your email confirmation code’. Check your spam or junk folder – if it still has not arrived, you can $linkText1 or $linkText2."

  val view: capture_email_passcode = app.injector.instanceOf[capture_email_passcode]

  "Capture Email Passcode Page" when {
    "the user is not a transactor" when {
      val isTransactor: Boolean = false
      "this is a normal flow" must {
        val isNewPasscode: Boolean = false
        implicit val doc: Document = Jsoup.parse(view(testEmail, EmailPasscodeForm.form, isTransactor, isNewPasscode).body)

        "have a backlink" in new ViewSetup {
          doc.hasBackLink mustBe true
        }

        "have the correct title" in new ViewSetup {
          doc.title() mustBe title
        }

        "have the correct heading" in new ViewSetup {
          doc.heading mustBe Some(heading)
        }

        "have the correct paragraph" in new ViewSetup {
          doc.para(1) mustBe Some(paragraph)
        }

        "have the correct inset text" in new ViewSetup {
          doc.panelIndent(1) mustBe Some(insetText)
        }

        "have the correct label" in new ViewSetup {
          doc.textBox("email-passcode") mustBe Some(label)
        }

        "have the correct hint" in new ViewSetup {
          doc.hintText mustBe Some(hint)
        }

        "have the correct button" in new ViewSetup {
          doc.submitButton mustBe Some(buttonText)
        }

        "have the correct details block" in new ViewSetup {
          doc.details mustBe Some(Details(
            detailsSummary,
            detailsContent
          ))
        }

        "have the correct links" in new ViewSetup {
          doc.link(1) mustBe Some(Link(linkText1, applicantRoutes.CaptureEmailPasscodeController.requestNew.url))
          doc.link(2) mustBe Some(Link(linkText2, applicantRoutes.CaptureEmailAddressController.show.url))
        }
      }

      "this is a new passcode flow" must {
        val isNewPasscode: Boolean = true
        implicit val doc: Document = Jsoup.parse(view(testEmail, EmailPasscodeForm.form, isTransactor, isNewPasscode).body)

        "have a backlink" in new ViewSetup {
          doc.hasBackLink mustBe true
        }

        "have the correct title" in new ViewSetup {
          doc.title() mustBe titleNewPasscode
        }

        "have the correct heading" in new ViewSetup {
          doc.heading mustBe Some(headingNewPasscode)
        }

        "have the correct paragraph" in new ViewSetup {
          doc.para(1) mustBe Some(paragraphNewPasscode)
        }

        "have the correct inset text" in new ViewSetup {
          doc.panelIndent(1) mustBe Some(insetText)
        }

        "have the correct label" in new ViewSetup {
          doc.textBox("email-passcode") mustBe Some(label)
        }

        "have the correct hint" in new ViewSetup {
          doc.hintText mustBe Some(hint)
        }

        "have the correct button" in new ViewSetup {
          doc.submitButton mustBe Some(buttonText)
        }

        "have the correct details block" in new ViewSetup {
          doc.details mustBe Some(Details(
            detailsSummary,
            detailsContent
          ))
        }

        "have the correct links" in new ViewSetup {
          doc.link(1) mustBe Some(Link(linkText1, applicantRoutes.CaptureEmailPasscodeController.requestNew.url))
          doc.link(2) mustBe Some(Link(linkText2, applicantRoutes.CaptureEmailAddressController.show.url))
        }
      }
    }
    "the user is a transactor" when {
      val isTransactor: Boolean = true
      "this is a normal flow" must {
        val isNewPasscode: Boolean = false
        implicit val doc: Document = Jsoup.parse(view(testEmail, EmailPasscodeForm.form, isTransactor, isNewPasscode).body)

        "have a backlink" in new ViewSetup {
          doc.hasBackLink mustBe true
        }

        "have the correct title" in new ViewSetup {
          doc.title() mustBe title
        }

        "have the correct heading" in new ViewSetup {
          doc.heading mustBe Some(heading)
        }

        "have the correct paragraph" in new ViewSetup {
          doc.para(1) mustBe Some(paragraph)
        }

        "have the correct inset text" in new ViewSetup {
          doc.panelIndent(1) mustBe Some(insetText)
        }

        "have the correct label" in new ViewSetup {
          doc.textBox("email-passcode") mustBe Some(label)
        }

        "have the correct hint" in new ViewSetup {
          doc.hintText mustBe Some(hint)
        }

        "have the correct button" in new ViewSetup {
          doc.submitButton mustBe Some(buttonText)
        }

        "have the correct details block" in new ViewSetup {
          doc.details mustBe Some(Details(
            detailsSummary,
            detailsContent
          ))
        }

        "have the correct links" in new ViewSetup {
          doc.link(1) mustBe Some(Link(linkText1, transactorRoutes.TransactorCaptureEmailPasscodeController.requestNew.url))
          doc.link(2) mustBe Some(Link(linkText2, transactorRoutes.TransactorCaptureEmailAddressController.show.url))
        }
      }

      "this is a new passcode flow" must {
        val isNewPasscode: Boolean = true
        implicit val doc: Document = Jsoup.parse(view(testEmail, EmailPasscodeForm.form, isTransactor, isNewPasscode).body)

        "have a backlink" in new ViewSetup {
          doc.hasBackLink mustBe true
        }

        "have the correct title" in new ViewSetup {
          doc.title() mustBe titleNewPasscode
        }

        "have the correct heading" in new ViewSetup {
          doc.heading mustBe Some(headingNewPasscode)
        }

        "have the correct paragraph" in new ViewSetup {
          doc.para(1) mustBe Some(paragraphNewPasscode)
        }

        "have the correct inset text" in new ViewSetup {
          doc.panelIndent(1) mustBe Some(insetText)
        }

        "have the correct label" in new ViewSetup {
          doc.textBox("email-passcode") mustBe Some(label)
        }

        "have the correct hint" in new ViewSetup {
          doc.hintText mustBe Some(hint)
        }

        "have the correct button" in new ViewSetup {
          doc.submitButton mustBe Some(buttonText)
        }

        "have the correct details block" in new ViewSetup {
          doc.details mustBe Some(Details(
            detailsSummary,
            detailsContent
          ))
        }

        "have the correct links" in new ViewSetup {
          doc.link(1) mustBe Some(Link(linkText1, transactorRoutes.TransactorCaptureEmailPasscodeController.requestNew.url))
          doc.link(2) mustBe Some(Link(linkText2, transactorRoutes.TransactorCaptureEmailAddressController.show.url))
        }
      }
    }
  }
}
