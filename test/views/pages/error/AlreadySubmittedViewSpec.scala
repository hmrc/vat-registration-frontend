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
import views.html.pages.error.AlreadySubmittedKickout

class AlreadySubmittedViewSpec extends VatRegViewSpec {
  object ExpectedContent {
    val heading = "We have already received a VAT registration application for this business"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "Applications can take up to 30 days to process, you should wait for this period of time before contacting us."
    val button = "Sign out"
  }

  val view: AlreadySubmittedKickout = app.injector.instanceOf[AlreadySubmittedKickout]

  implicit val doc: Document = Jsoup.parse(view().body)

  "Already Submitted page" must {
    "not have a back link" in new ViewSetup {
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
    }

    "have a sign out button" in new ViewSetup {
      doc.select(Selectors.button).text() mustBe ExpectedContent.button
    }
  }
}
