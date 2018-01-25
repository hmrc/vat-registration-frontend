/*
 * Copyright 2018 HM Revenue & Customs
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
import models.api._
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import models.view.vatFinancials.ZeroRatedSales.ZERO_RATED_SALES_YES
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}
import org.scalatest.Inspectors
import uk.gov.hmrc.play.test.UnitSpec

class S4LModelsSpec  extends UnitSpec with Inspectors with VatRegistrationFixture {

  "S4LVatFinancials.S4LApiTransformer.toApi" should {

    val s4l = S4LVatFinancials(
      zeroRatedTurnover = Some(ZeroRatedSales(ZERO_RATED_SALES_YES)),
      zeroRatedTurnoverEstimate = Some(EstimateZeroRatedSales(1))
    )

    "transform complete S4L model to API" in {
      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4l) shouldBe expected
    }

    "transform valid partial S4L model to API" in {
      val s4lWithoutAccountingPeriod = s4l.copy()

      val expected = VatFinancials(
        zeroRatedTurnoverEstimate = Some(1)
      )

      S4LVatFinancials.apiT.toApi(s4lWithoutAccountingPeriod) shouldBe expected
    }
  }

  "S4LVatContact.S4LModelTransformer.toApi" should {

    val s4l = S4LVatContact(
      businessContactDetails = Some(BusinessContactDetails(
        email = "email",
        daytimePhone = Some("tel"),
        mobile = Some("mobile"),
        website = Some("website"))),
      ppob = Some(PpobView(scrsAddress.id, Some(scrsAddress)))
    )

    "transform complete s4l container to API" in {

      val expected = VatContact(
        digitalContact = VatDigitalContact(
          email = "email",
          tel = Some("tel"),
          mobile = Some("mobile")),
        website = Some("website"),
        ppob = scrsAddress)

      S4LVatContact.apiT.toApi(s4l) shouldBe expected

    }

    "transform s4l container with incomplete data error" in {
      val s4lNoContactDetails = s4l.copy(businessContactDetails = None)
      an[IllegalStateException] should be thrownBy S4LVatContact.apiT.toApi(s4lNoContactDetails)

      val s4lPpob = s4l.copy(ppob = None)
      an[IllegalStateException] should be thrownBy S4LVatContact.apiT.toApi(s4lPpob)
    }
  }

  "S4LVatContact.S4LModelTransformer.toS4LModel" should {

    "transform API to S4L model" in {
      val vs = emptyVatScheme.copy(vatContact = Some(
        VatContact(
          digitalContact = VatDigitalContact(email = "email", tel = Some("tel"), mobile = Some("mobile")),
          website = Some("website"),
          ppob = scrsAddress)))

      val expected = S4LVatContact(
        businessContactDetails = Some(BusinessContactDetails(
                                      email = "email",
                                      daytimePhone = Some("tel"),
                                      mobile = Some("mobile"),
                                      website = Some("website"))),
        ppob = Some(PpobView(scrsAddress.id, Some(scrsAddress)))
      )


      S4LVatContact.modelT.toS4LModel(vs) shouldBe expected
    }
  }
}
