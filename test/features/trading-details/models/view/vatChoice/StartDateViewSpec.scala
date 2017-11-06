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
import models.{DateModel, S4LTradingDetails}
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

  "ViewModelFormat" should {
    val s4LTradingDetails: S4LTradingDetails = S4LTradingDetails(startDate = Some(validStartDateView))

    "extract startDate from vatTradingDetails" in {
      StartDateView.viewModelFormat.read(s4LTradingDetails) shouldBe Some(validStartDateView)
    }

    "update empty vatContact with startDate" in {
      StartDateView.viewModelFormat.update(validStartDateView, Option.empty[S4LTradingDetails]).startDate shouldBe Some(validStartDateView)
    }

    "update non-empty vatContact with startDate" in {
      StartDateView.viewModelFormat.update(validStartDateView, Some(s4LTradingDetails)).startDate shouldBe Some(validStartDateView)
    }

  }

}
