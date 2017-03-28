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

package models.view

import java.time.LocalDate

import fixtures.VatRegistrationFixture
import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api.{VatChoice, VatScheme}
import models.view.vatTradingDetails.StartDateView
import models.{ApiModelTransformer, DateModel, ViewModelTransformer}
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
      val vatChoice = VatChoice(newStartDate.date.get, NECESSITY_OBLIGATORY)
      ViewModelTransformer[StartDateView, VatChoice]
        .toApi(startDate, vatChoice) shouldBe VatChoice(startDate.date.get, NECESSITY_OBLIGATORY)
    }

    "use DEFAULT date when no date present" in {
      val vatChoice = VatChoice(newStartDate.date.get, NECESSITY_OBLIGATORY)

      ViewModelTransformer[StartDateView, VatChoice]
        .toApi(StartDateView("any", None),vatChoice ) shouldBe vatChoice.copy(startDate = StartDateView.DEFAULT_DATE)
    }
  }

  "apply" should {
    "extract a StartDate from a VatScheme" in {
      val vatChoice = VatChoice(startDate.date.get, NECESSITY_VOLUNTARY)
      val vatScheme = VatScheme(id = validRegId, vatChoice = Some(vatChoice))
      ApiModelTransformer[StartDateView].toViewModel(vatScheme) shouldBe Some(startDate)
    }

    "extract a default StartDate from a VatScheme that has no VatChoice " in {
      val vatScheme = VatScheme(id = validRegId, vatChoice = None)
      ApiModelTransformer[StartDateView].toViewModel(vatScheme) shouldBe None
    }
  }

  //TODO this is testing a TODO'ed piece of code - remove asap
  "fromLocalDate" should {
    "craete a COMPANY_REGISTRATION_DATE StartDate if input LocalDate is 1/1/1970" in {
      StartDateView.fromLocalDate(LocalDate.of(1970, 1, 1)) shouldBe StartDateView(StartDateView.COMPANY_REGISTRATION_DATE)
    }
  }

}
