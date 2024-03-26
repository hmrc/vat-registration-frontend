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

package views.applicant

import org.jsoup.Jsoup
import views.VatRegViewSpec
import views.html.applicant.EmailVerified

class EmailVerifiedViewSpec extends VatRegViewSpec {

  val title = "Email address confirmed - Register for VAT - GOV.UK"
  val heading = "Email address confirmed"
  val buttonText = "Save and continue"

  "Email Verified Page" should {
    val view = app.injector.instanceOf[EmailVerified].apply(testCall)

    implicit val doc = Jsoup.parse(view.body)

    "have the correct title" in new ViewSetup {
      doc.title() mustBe title
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }

  }

}
