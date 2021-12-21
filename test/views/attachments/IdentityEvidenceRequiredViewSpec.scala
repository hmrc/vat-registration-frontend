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
import views.VatRegViewSpec
import views.html.IdentityEvidenceRequired

class IdentityEvidenceRequiredViewSpec extends VatRegViewSpec {

  val view = app.injector.instanceOf[IdentityEvidenceRequired]
  implicit val doc = Jsoup.parse(view().body)

  object ExpectedContent {
    val heading = "We require three pieces of additional information from you so we can prove your identity"
    val title = s"$heading - Register for VAT - GOV.UK"
    val para1 = "We need one piece of primary evidence which consists of a copy of government issued photo which could include:"
    val bullet1 = "a passport"
    val bullet2 = "a photo drivers licence"
    val bullet3 = "a national identity card"
    val para2 = "We need two additional pieces of evidence which can be copies of:"
    val additionalBullet1 = "a mortgage statement"
    val additionalBullet2 = "a lease or rental agreement"
    val additionalBullet3 = "a work permit or Visa"
    val additionalBullet4 = "any correspondence from the Department for Work and Pensions confirming entitlement to benefits"
    val additionalBullet5 = "a recent utility bill"
    val additionalBullet6 = "a birth certificate"
    val continue = "Save and continue"
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
      doc.para(1) mustBe Some(ExpectedContent.para1)
      doc.para(2) mustBe Some(ExpectedContent.para2)
    }

    "have the correct bullet list" in new ViewSetup {
      doc.unorderedList(1) mustBe List(
        ExpectedContent.bullet1,
        ExpectedContent.bullet2,
        ExpectedContent.bullet3
      )
    }

    "have the correct additional bullet list" in new ViewSetup {
      doc.unorderedList(2) mustBe List(
        ExpectedContent.additionalBullet1,
        ExpectedContent.additionalBullet2,
        ExpectedContent.additionalBullet3,
        ExpectedContent.additionalBullet4,
        ExpectedContent.additionalBullet5,
        ExpectedContent.additionalBullet6
      )
    }

    "have a primary action" in new ViewSetup {
      doc.submitButton mustBe Some(ExpectedContent.continue)
    }
  }

}
