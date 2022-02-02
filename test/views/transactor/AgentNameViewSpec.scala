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

package views.transactor

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import views.VatRegViewSpec
import views.html.transactor.AgentNameView
import play.api.data.Forms._

class AgentNameViewSpec extends VatRegViewSpec {

  object ExpectedMessages {
    val heading = "What is your name?"
    val firstNameLabel = "First name"
    val lastNameLabel = "Last name"
  }

  val testForm = Form(
    mapping(
      "firstName" -> text,
      "lastName" -> text
    )(Tuple2.apply)(Tuple2.unapply)
  )

  val view = app.injector.instanceOf[AgentNameView]
  implicit val doc: Document = Jsoup.parse(view(testForm).body)

  "the Agent Name page" must {
    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(ExpectedMessages.heading)
    }
    "have a firstName field" in new ViewSetup {
      doc.textBox("firstName") mustBe Some(ExpectedMessages.firstNameLabel)
    }
    "have a lastName field" in new ViewSetup {
      doc.textBox("lastName") mustBe Some(ExpectedMessages.lastNameLabel)
    }
    "have a submit button" in new ViewSetup {
      doc.submitButton.isDefined mustBe true
    }
  }

}
