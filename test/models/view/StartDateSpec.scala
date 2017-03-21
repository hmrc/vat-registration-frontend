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
import models.view.vatChoice.StartDate
import models.{ApiModelTransformer, ViewModelTransformer}
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.play.test.UnitSpec

class StartDateSpec extends UnitSpec with VatRegistrationFixture {

  val date = LocalDate.of(2017,3,21)
  val startDate = StartDate(StartDate.SPECIFIC_DATE, Some(date))
  val newStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(date))

  "toApi" should {
    "update a VatChoice a new StartDate" in {
      val vatChoice = VatChoice(newStartDate.date.get, NECESSITY_OBLIGATORY)
      ViewModelTransformer[StartDate, VatChoice]
        .toApi(startDate, vatChoice) shouldBe VatChoice(startDate.date.get, NECESSITY_OBLIGATORY)
    }
  }

  "apply" should {
    "extract a StartDate from a VatScheme" in {
      val vatChoice = VatChoice(startDate.date.get, NECESSITY_VOLUNTARY)
      val vatScheme = VatScheme(id = validRegId, vatChoice = Some(vatChoice))
      ApiModelTransformer[StartDate].toViewModel(vatScheme) shouldBe Some(startDate)
    }

    "extract a default StartDate from a VatScheme that has no VatChoice " in {
      val vatScheme = VatScheme(id = validRegId, vatChoice = None)
      ApiModelTransformer[StartDate].toViewModel(vatScheme) shouldBe None
    }
  }

}
