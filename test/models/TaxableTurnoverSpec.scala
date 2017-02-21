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
import models.view.TaxableTurnover
import models.view.TaxableTurnover._
import org.joda.time.DateTime
import uk.gov.hmrc.play.test.UnitSpec

class TaxableTurnoverSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty TaxableTurnover.model" in {
      TaxableTurnover.empty shouldBe TaxableTurnover("")
    }
  }

  "apply" should {
    "convert a VatChoice (Obligatory) to view model" in {
      val vatSchemeObligatory = VatScheme(
        validRegId,
        vatChoice = Some(VatChoice(
          DateTime.now,
          VatChoice.NECESSITY_OBLIGATORY
        ))
      )
      TaxableTurnover.apply(vatSchemeObligatory) shouldBe TaxableTurnover(TAXABLE_YES)
    }

    "convert a VatChoice (Voluntary) to view model" in {
      val vatSchemeVolunatary = VatScheme(
        validRegId,
        vatChoice = Some(VatChoice(
          DateTime.now,
          VatChoice.NECESSITY_VOLUNTARY
        ))
      )
      TaxableTurnover.apply(vatSchemeVolunatary) shouldBe TaxableTurnover(TAXABLE_NO)
    }

    "convert an invalid VatChoice to empty view model" in {
      val vatSchemeVolunatary = VatScheme(validRegId, vatChoice = Some(VatChoice(DateTime.now, "GARBAGE")))
      TaxableTurnover.apply(vatSchemeVolunatary) shouldBe TaxableTurnover.empty
    }

    "convert a none VatChoice to empty view model" in {
      val vatSchemeVolunatary = VatScheme(validRegId)
      TaxableTurnover.apply(vatSchemeVolunatary) shouldBe TaxableTurnover.empty
    }

  }

}

