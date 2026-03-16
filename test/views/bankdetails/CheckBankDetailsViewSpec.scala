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

package views.bankdetails

import models.BankAccountDetails
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.VatRegViewSpec
import views.html.bankdetails.CheckBankDetailsView

class CheckBankDetailsViewSpec extends VatRegViewSpec {

  val view: CheckBankDetailsView = app.injector.instanceOf[CheckBankDetailsView]

  val title              = "Check your account details"
  val heading            = "Check your account details"
  val cardTitle          = "Bank account details"
  val changeLink         = "Change"
  val accountNameLabel   = "Account name"
  val accountNumberLabel = "Account number"
  val sortCodeLabel      = "Sort code"
  val rollNumberLabel    = "Building society roll number"
  val p1                 = "By confirming these account details, you agree the information you have provided is complete and correct."
  val buttonText         = "Confirm and continue"

  val bankDetails: BankAccountDetails = BankAccountDetails(
    name       = "Test Account",
    sortCode   = "123456",
    number     = "12345678",
    rollNumber = None,
    status     = None
  )

  val bankDetailsWithRollNumber: BankAccountDetails = bankDetails.copy(rollNumber = Some("AB/121212"))

  "CheckBankDetailsView" should {

    implicit lazy val doc: Document = Jsoup.parse(view(bankDetails).body)

    "have the correct title" in new ViewSetup {
      doc.title must include(title)
    }

    "have the correct heading" in new ViewSetup {
      doc.heading mustBe Some(heading)
    }

    "have the correct card title" in new ViewSetup {
      doc.select(".govuk-summary-card__title").text mustBe cardTitle
    }

    "have a change link" in new ViewSetup {
      doc.select(".govuk-summary-card__actions a").text must include(changeLink)
    }

    "have the correct account name label" in new ViewSetup {
      doc.select(".govuk-summary-list__key").get(0).text mustBe accountNameLabel
    }

    "have the correct account name value" in new ViewSetup {
      doc.select(".govuk-summary-list__value").get(0).text mustBe "Test Account"
    }

    "have the correct account number label" in new ViewSetup {
      doc.select(".govuk-summary-list__key").get(1).text mustBe accountNumberLabel
    }

    "have the correct account number value" in new ViewSetup {
      doc.select(".govuk-summary-list__value").get(1).text mustBe "12345678"
    }

    "have the correct sort code label" in new ViewSetup {
      doc.select(".govuk-summary-list__key").get(2).text mustBe sortCodeLabel
    }

    "have the correct sort code value" in new ViewSetup {
      doc.select(".govuk-summary-list__value").get(2).text mustBe "123456"
    }

    "not show roll number row when roll number is absent" in new ViewSetup {
      doc.select(".govuk-summary-list__key").size mustBe 3
    }

    "have the correct p1" in new ViewSetup {
      doc.para(1) mustBe Some(p1)
    }

    "have the correct continue button" in new ViewSetup {
      doc.submitButton mustBe Some(buttonText)
    }
  }

  "CheckBankDetailsView with roll number" should {

    implicit lazy val doc: Document = Jsoup.parse(view(bankDetailsWithRollNumber).body)

    "show the roll number row when roll number is present" in new ViewSetup {
      doc.select(".govuk-summary-list__key").size mustBe 4
    }

    "have the correct roll number label" in new ViewSetup {
      doc.select(".govuk-summary-list__key").get(3).text mustBe rollNumberLabel
    }

    "have the correct roll number value" in new ViewSetup {
      doc.select(".govuk-summary-list__value").get(3).text mustBe "AB/121212"
    }
  }
}