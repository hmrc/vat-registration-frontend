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

package models

import fixtures.VatRegistrationFixture
import models.api.{VatChoice, VatScheme}
import models.view.VoluntaryRegistration
import models.view.VoluntaryRegistration._
import uk.gov.hmrc.play.test.UnitSpec

class VoluntaryRegistrationSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty VoluntaryRegistration model" in {
      VoluntaryRegistration.empty shouldBe VoluntaryRegistration("")
    }
  }

  "toApi" should {
    "upserts (merge) a current VatChoice API model with the details of an instance of VoluntaryRegistration view model" in {
      val vatChoiceObligatory = VatChoice(
        validStartDate.toDateTime,
        VatChoice.NECESSITY_OBLIGATORY
      )

      VoluntaryRegistration(REGISTER_YES).toApi(vatChoiceObligatory) shouldBe VatChoice(
        validStartDate.toDateTime,
        VatChoice.NECESSITY_VOLUNTARY
      )

      val vatChoiceVoluntary = VatChoice(
        validStartDate.toDateTime,
        VatChoice.NECESSITY_VOLUNTARY
      )

      VoluntaryRegistration(REGISTER_NO).toApi(vatChoiceVoluntary) shouldBe VatChoice(
        validStartDate.toDateTime,
        VatChoice.NECESSITY_OBLIGATORY
      )
    }
  }

  "apply" should {

    val vatChoiceVoluntary = VatChoice(
      validStartDate.toDateTime,
      VatChoice.NECESSITY_VOLUNTARY
    )

    val vatChoiceObligatory = VatChoice(
      validStartDate.toDateTime,
      VatChoice.NECESSITY_OBLIGATORY
    )

    val vatScheme = VatScheme(
      validRegId, None, None, None
    )

    "convert voluntary vatChoice to view model" in {
      val vs = vatScheme.copy(vatChoice = Some(vatChoiceVoluntary))
      VoluntaryRegistration.apply(vs) shouldBe VoluntaryRegistration(REGISTER_YES)
    }

    "convert obligatory vatChoice to empty view model" in {
      val vs = vatScheme.copy(vatChoice = Some(vatChoiceObligatory))
      VoluntaryRegistration.apply(vs) shouldBe VoluntaryRegistration.empty
    }

    "convert none vatChoice to view empty model" in {
      val vs = vatScheme.copy(vatChoice = None)
      VoluntaryRegistration.apply(vs) shouldBe VoluntaryRegistration.empty
    }

  }
}
