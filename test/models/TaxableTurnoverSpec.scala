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
import models.api.{VatChoice, VatFinancials, VatScheme}
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
    "convert a populated VatScheme's VatChoice API model to an instance of TaxableTurnover.view model" in {
      val vatSchemeObligatory = VatScheme(
        validRegId,
        Some(validVatTradingDetails),
        Some(VatChoice(
          DateTime.now,
          VatChoice.NECESSITY_OBLIGATORY
        )),
        Some(validVatFinancials)
      )

      TaxableTurnover.apply(vatSchemeObligatory) shouldBe TaxableTurnover(TAXABLE_YES)
      TaxableTurnover.apply(validVatScheme) shouldBe TaxableTurnover(TAXABLE_NO)
    }
  }
}

