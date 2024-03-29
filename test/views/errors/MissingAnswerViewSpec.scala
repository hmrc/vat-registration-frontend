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

package views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.errors.MissingAnswer

class MissingAnswerViewSpec extends VatRegViewSpec {

  object ExpectedMessages {
    val title = "You need to provide more information"
    val p = "You are missing information from the following section of your application:"
    val missingAnswer = "Registration reason"
    val p2 = "You need to provide this before you can continue"
    val link = "Return to application"
  }

  val sectionHeadingKey = "tasklist.eligibilty.regReason"

  val view = app.injector.instanceOf[MissingAnswer]
  implicit val doc: Document = Jsoup.parse(view(sectionHeadingKey).body)

  "The missing answer page" must {
    "have the correct page title" in new ViewSetup {
      doc.title must include(ExpectedMessages.title)
    }
    "have the correct h1 heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.title)
    }
    "have the correct first paragraph" in new ViewSetup {
      doc.para(1) mustBe Some(ExpectedMessages.p)
    }
    "show the correct section for the missing answer" in new ViewSetup {
      doc.unorderedList(1) mustBe List(ExpectedMessages.missingAnswer)
    }
    "have the correct second paragraph" in new ViewSetup {
      doc.para(2) mustBe Some(ExpectedMessages.p2)
    }
  }

}
