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

package views.pages.error

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.pages.error.SubmissionRetryableView

class SubmissionRetryableViewSpec extends VatRegViewSpec {
  object ExpectedContent {
    val heading = "We couldn’t process your application"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "Sorry, there is a technical problem and we couldn’t process your application. Any details you entered have been saved."
    val para2 = "Please resend your application."
    val resend =  "Resend"
  }

  val view = app.injector.instanceOf[SubmissionRetryableView]

  implicit val doc: Document = Jsoup.parse(view().body)

  "Submission Timout page" must {
    "not has back link" in new ViewSetup {
      doc.hasBackLink mustBe false
    }

    "has the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedContent.heading)
    }

    "has the correct page title" in new ViewSetup {
      doc.title mustBe ExpectedContent.title
    }

    "have correct text" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedContent.para1)
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "has a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.resend)
    }
  }
}
