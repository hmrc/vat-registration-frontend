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

import models.api.{IdentityEvidence, VAT2}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.MultipleDocumentsRequired
import scala.collection.JavaConverters._

class MultipleDocumentsRequiredViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[MultipleDocumentsRequired]
  implicit val doc = Jsoup.parse(view(List(IdentityEvidence, VAT2)).body)

  object ExpectedContent {
    val heading = "We require some additional information from you"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para = "To enable us to progress your application further, we need the following information and documents from you:"
    val bullet1 = "identity documents of you"
    val linkText = "VAT2 form (opens in new tab)"
    val bullet2 = s"a completed $linkText"
    val continue = "Save and continue"
  }

  object IdentityDetails {
    val summary = "What identity documents can I provide?"
    val content = "We need one piece of primary evidence which consists of a copy of government issued photo which could include: " +
      "a passport " +
      "a photo drivers licence " +
      "a national identity card " +
      "We need two additional pieces of evidence which can be copies of: " +
      "a mortgage statement " +
      "a lease or rental agreement " +
      "a work permit or Visa " +
      "any correspondence from the Department for Work and Pensions confirming entitlement to benefits " +
      "a recent utility bill " +
      "a birth certificate"
  }

  "The charge expectancy (regularly claim refunds) page" must {
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
      doc.para(1) mustBe Some(ExpectedContent.para)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2
      )
    }

    "have the correct link" in new ViewSetup {
      doc.link(1) mustBe Some(Link(ExpectedContent.linkText, appConfig.vat2Link))
    }

    "have a details block" in new ViewSetup {
      doc.details mustBe Some(Details(IdentityDetails.summary, IdentityDetails.content))
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
