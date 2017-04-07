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
import models.api.{VatEuTrading, VatTradingDetails}
import models.view.vatTradingDetails.vatEuTrading.ApplyEori
import models.{ApiModelTransformer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class ApplyEoriSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "toApi" should {
    val applyEori = ApplyEori(ApplyEori.APPLY_EORI_YES)

    val vatTradingDetails = VatTradingDetails(
      validVatChoice,
      validTradingName,
      validEuTrading
    )

    val differentVatTradingDetails = VatTradingDetails(
      validVatChoice,
      validTradingName,
      VatEuTrading(false, Some(true))
    )

    "update VatFinancials with new AccountingPeriod" in {
      ViewModelTransformer[ApplyEori, VatTradingDetails]
        .toApi(applyEori, validVatTradingDetails) shouldBe differentVatTradingDetails
    }
  }

  "apply" should {

    "convert VatScheme without VatTradingDetails to empty view model" in {
      val vs = vatScheme(vatTradingDetails = None)
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with VatEuTrading section to view model - ApplyEori Yes" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(eoriApplication = Some(true))))
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe Some(ApplyEori(ApplyEori.APPLY_EORI_YES))
    }

    "convert VatScheme with VatEuTrading section to view model - ApplyEori No" in {
      val vs = vatScheme(vatTradingDetails = Some(tradingDetails(eoriApplication = Some(false))))
      ApiModelTransformer[ApplyEori].toViewModel(vs) shouldBe Some(ApplyEori(ApplyEori.APPLY_EORI_NO))
    }

  }
}

