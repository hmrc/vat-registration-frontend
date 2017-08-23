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
import models.api.VatFlatRateScheme
import models.view.frs.FrsStartDateView
import models.{ApiModelTransformer, DateModel, S4LFlatRateScheme}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class FrsStartDateViewSpec extends UnitSpec with VatRegistrationFixture with Inside {

  val date = LocalDate.of(2017, 3, 21)
  val differentStartDateView = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date))
  val registrationdDateView = FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE, Some(date))


  "unbind" should {
    "decompose a COMPANY_REGISTRATION_DATE StartDate" in {
      val testStartDate = FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE)
      inside(FrsStartDateView.unbind(testStartDate)) {
        case Some((dateChoice, odm)) =>
          dateChoice shouldBe FrsStartDateView.VAT_REGISTRATION_DATE
          odm shouldBe None
      }
    }

    "decompose a DIFFERENT_DATE StartDate" in {
      val testStartDate = FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date))
      inside(FrsStartDateView.unbind(testStartDate)) {
        case Some((dateChoice, odm)) =>
          dateChoice shouldBe FrsStartDateView.DIFFERENT_DATE
          odm shouldBe Some(DateModel.fromLocalDate(date))
      }
    }
  }

  "bind" should {
    "create StartDate when DateModel is present" in {
      FrsStartDateView.bind("any", Some(DateModel.fromLocalDate(date))) shouldBe FrsStartDateView("any", Some(date))
    }
    "create StartDate when DateModel is NOT present" in {
      FrsStartDateView.bind("any", None) shouldBe FrsStartDateView("any", None)
    }
  }

  "ViewModelFormat" should {
    val validFrsStartDateView = FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE)
    val s4LFlatRateScheme: S4LFlatRateScheme = S4LFlatRateScheme(frsStartDate = Some(validFrsStartDateView))

    "extract startDate from vatTradingDetails" in {
      FrsStartDateView.viewModelFormat.read(s4LFlatRateScheme) shouldBe Some(validFrsStartDateView)
    }

    "update empty vatContact with startDate" in {
      FrsStartDateView.viewModelFormat.update(validFrsStartDateView, Option.empty[S4LFlatRateScheme]).frsStartDate shouldBe Some(validFrsStartDateView)
    }

    "update non-empty vatContact with startDate" in {
      FrsStartDateView.viewModelFormat.update(validFrsStartDateView, Some(s4LFlatRateScheme)).frsStartDate shouldBe Some(validFrsStartDateView)
    }

    "ApiModelTransformer" should {

      "produce empty view model from an empty frs start date" in {
        val vm = ApiModelTransformer[FrsStartDateView]
          .toViewModel(vatScheme(vatFlatRateScheme = None))
        vm shouldBe None
      }

      "produce a view model from a vatScheme with registration date" in {
        val vm = ApiModelTransformer[FrsStartDateView]
          .toViewModel(vatScheme(vatFlatRateScheme = Some(VatFlatRateScheme(whenDoYouWantToJoinFrs = Some(FrsStartDateView.VAT_REGISTRATION_DATE),startDate = Some(date)))))
        vm shouldBe Some(FrsStartDateView(FrsStartDateView.VAT_REGISTRATION_DATE, Some(date)))
      }

      "produce a view model from a vatScheme with different date" in {
        val vm = ApiModelTransformer[FrsStartDateView]
          .toViewModel(vatScheme(vatFlatRateScheme = Some(VatFlatRateScheme(whenDoYouWantToJoinFrs = Some(FrsStartDateView.DIFFERENT_DATE),startDate = Some(date)))))
        vm shouldBe Some(FrsStartDateView(FrsStartDateView.DIFFERENT_DATE, Some(date)))
      }

    }
  }

}
