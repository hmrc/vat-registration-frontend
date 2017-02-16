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
import models.api.VatChoice
import models.view.{StartDate, VoluntaryRegistration}
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
    "convert a populated VatScheme's VatChoice API model to an instance of VoluntaryRegistration view model" in {
      VoluntaryRegistration.apply(validVatScheme) shouldBe VoluntaryRegistration(REGISTER_YES)
    }
//
//    "convert a populated VatScheme's VatChoice API model that has an empty trading name to an instance of VoluntaryRegistration view model" in {
//      VoluntaryRegistration.apply(validVatSchemeEmptyVoluntaryRegistration) shouldBe VoluntaryRegistration(yesNo = TRADING_NAME_NO, tradingName = None)
//    }
//
//    "convert a populated VatScheme's VatChoice API model that has a hash as a trading name to an instance of VoluntaryRegistration view model" in {
//      VoluntaryRegistration.apply(validVatSchemeHashInVoluntaryRegistration) shouldBe VoluntaryRegistration.empty
//    }
  }
}
