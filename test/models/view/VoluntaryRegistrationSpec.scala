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

import fixtures.VatRegistrationFixture
import models.api.VatChoice.{NECESSITY_OBLIGATORY, NECESSITY_VOLUNTARY}
import models.api.{VatChoice, VatScheme}
import models.view.VoluntaryRegistration._
import models.{ApiModelTransformer, ViewModelTransformer}
import uk.gov.hmrc.play.test.UnitSpec

class VoluntaryRegistrationSpec extends UnitSpec with VatRegistrationFixture {

  "toApi" should {
    "update a VatChoice with new VoluntaryRegistration (YES)" in {
      val vatChoiceObligatory = VatChoice(validStartDate.toDateTime, NECESSITY_OBLIGATORY)
      ViewModelTransformer[VoluntaryRegistration, VatChoice]
        .toApi(VoluntaryRegistration(REGISTER_YES), vatChoiceObligatory) shouldBe VatChoice(validStartDate.toDateTime, NECESSITY_VOLUNTARY)
    }

    "update a VatChoice with new VoluntaryRegistration (NO)" in {
      val vatChoiceVoluntary = VatChoice(validStartDate.toDateTime, NECESSITY_VOLUNTARY)
      ViewModelTransformer[VoluntaryRegistration, VatChoice]
        .toApi(VoluntaryRegistration(REGISTER_NO), vatChoiceVoluntary) shouldBe VatChoice(validStartDate.toDateTime, NECESSITY_OBLIGATORY)
    }
  }

  "apply" should {
    val vatChoiceVoluntary = VatChoice(validStartDate.toDateTime, NECESSITY_VOLUNTARY)
    val vatChoiceObligatory = VatChoice(validStartDate.toDateTime, NECESSITY_OBLIGATORY)
    val vatScheme = VatScheme(validRegId)

    "convert voluntary vatChoice to view model" in {
      val vs = vatScheme.copy(vatChoice = Some(vatChoiceVoluntary))
      ApiModelTransformer[VoluntaryRegistration].toViewModel(vs) shouldBe Some(VoluntaryRegistration(REGISTER_YES))
    }

    "convert obligatory vatChoice to empty view model" in {
      val vs = vatScheme.copy(vatChoice = Some(vatChoiceObligatory))
      ApiModelTransformer[VoluntaryRegistration].toViewModel(vs) shouldBe None
    }

    "convert none vatChoice to view empty model" in {
      val vs = vatScheme.copy(vatChoice = None)
      ApiModelTransformer[VoluntaryRegistration].toViewModel(vs) shouldBe None
    }

  }
}
