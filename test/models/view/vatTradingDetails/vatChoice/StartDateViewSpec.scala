/*
 * Copyright 2017 HM Revenue & Customs
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

package models.view.vatTradingDetails.vatChoice

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api.VatTradingDetails
import models.{DateModel, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class StartDateViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val date = LocalDate.of(2017, 3, 21)
  val startDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(date))
  val newStartDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(date))

  "unbind" should {
    "decompose a COMPANY_REGISTRATION_DATE StartDate" in {
      val testStartDate = StartDateView(StartDateView.COMPANY_REGISTRATION_DATE)
      inside(StartDateView.unbind(testStartDate)) {
        case Some((dateChoice, odm)) =>
          dateChoice shouldBe StartDateView.COMPANY_REGISTRATION_DATE
          odm shouldBe None
      }
    }

    "decompose a SPECIFIC_DATE StartDate" in {
      val testStartDate = StartDateView(StartDateView.SPECIFIC_DATE, Some(date))
      inside(StartDateView.unbind(testStartDate)) {
        case Some((dateChoice, odm)) =>
          dateChoice shouldBe StartDateView.SPECIFIC_DATE
          odm shouldBe Some(DateModel.fromLocalDate(date))
      }
    }
  }

  "bind" should {
    "create StartDate when DateModel is present" in {
      StartDateView.bind("any", Some(DateModel.fromLocalDate(date))) shouldBe StartDateView("any", Some(date))
    }
    "create StartDate when DateModel is NOT present" in {
      StartDateView.bind("any", None) shouldBe StartDateView("any", None)
    }
  }

  "toApi" should {
    "update a VatChoice a new StartDate" in {
      val vtd = tradingDetails(startDate = newStartDate.date)
      inside(ViewModelTransformer[StartDateView, VatTradingDetails].toApi(startDate, vtd)) {
        case tradingDetails => tradingDetails.vatChoice.vatStartDate.startDate shouldBe newStartDate.date
      }
    }

    "when no date present, StardDateView contains date type selection" in {
      val c = StartDateView("from S4L", None)
      val g = tradingDetails()
      val transformed = ViewModelTransformer[StartDateView, VatTradingDetails].toApi(c, g)
      transformed shouldBe g.copy(vatChoice = g.vatChoice.copy(vatStartDate = g.vatChoice.vatStartDate.copy(selection = "from S4L")))
    }
  }

}
