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

package views.attachments

import org.jsoup.Jsoup
import play.twirl.api.Html
import views.VatRegViewSpec
import views.html.EmailDocuments

class EmailDocumentsViewSpec extends VatRegViewSpec {

  val emailDocumentsPage: EmailDocuments = app.injector.instanceOf[EmailDocuments]


  lazy val view: Html = emailDocumentsPage()
  implicit val doc = Jsoup.parse(view.body)

  object ExpectedContent {
    val heading = "We need you to send the documents to the following HMRC email address"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "For us to be able to progress your VAT registration quickly we need you to send these documents as soon as you have submitted the application form."
    val panel1 = "vrs.newregistrations@hmrc.gov.uk"
    val continue = "Save and continue"
  }

  "The Email Documents page" must {
    "have a back link in new Setup" in new ViewSetup {
      doc.hasBackLink mustBe true
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "have the correct title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have the correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
    }

    "have the correct panel text" in new ViewSetup {
      doc.select(Selectors.indent).get(0).text mustBe ExpectedContent.panel1
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
