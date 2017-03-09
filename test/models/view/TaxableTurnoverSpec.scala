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
import models.ApiModelTransformer
import models.api.{VatChoice, VatScheme}
import models.view.TaxableTurnover._
import org.joda.time.DateTime
import uk.gov.hmrc.play.test.UnitSpec

class TaxableTurnoverSpec extends UnitSpec with VatRegistrationFixture {

  "apply" should {
    "convert a VatChoice (Obligatory) to view model" in {
      val vatSchemeObligatory = VatScheme(
        validRegId,
        vatChoice = Some(VatChoice(
          DateTime.now,
          VatChoice.NECESSITY_OBLIGATORY
        ))
      )
      ApiModelTransformer[TaxableTurnover].toViewModel(vatSchemeObligatory) shouldBe Some(TaxableTurnover(TAXABLE_YES))
    }

    "convert a VatChoice (Voluntary) to view model" in {
      val vatSchemeVoluntary = VatScheme(
        validRegId,
        vatChoice = Some(VatChoice(
          DateTime.now,
          VatChoice.NECESSITY_VOLUNTARY
        ))
      )
      ApiModelTransformer[TaxableTurnover].toViewModel(vatSchemeVoluntary) shouldBe Some(TaxableTurnover(TAXABLE_NO))
    }

    "convert an invalid VatChoice to empty view model" in {
      val vatSchemeVoluntary = VatScheme(validRegId, vatChoice = Some(VatChoice(DateTime.now, "GARBAGE")))
      ApiModelTransformer[TaxableTurnover].toViewModel(vatSchemeVoluntary) shouldBe None
    }

    "convert a none VatChoice to empty view model" in {
      val vatSchemeVoluntary = VatScheme(validRegId)
      ApiModelTransformer[TaxableTurnover].toViewModel(vatSchemeVoluntary) shouldBe None
    }

  }

}

