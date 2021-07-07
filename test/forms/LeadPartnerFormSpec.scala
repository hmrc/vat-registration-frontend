/*
 * Copyright 2021 HM Revenue & Customs
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

package forms

import models.api.{CharitableOrg, Individual, LtdLiabilityPartnership, NonUkNonEstablished, PartyType, RegSociety, ScotLtdPartnership, ScotPartnership, UkCompany}
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import testHelpers.VatRegSpec

class LeadPartnerFormSpec extends VatRegSpec {

  val form: Form[PartyType] = LeadPartnerForm()

  "LeadPartnerForm" should {

    "successfully parse a valid entity" in {
      val validEntityTypes = Map(
        "Z1" -> Individual,
        "55" -> NonUkNonEstablished,
        "50" -> UkCompany,
        "58" -> ScotPartnership,
        "59" -> ScotLtdPartnership,
        "52" -> LtdLiabilityPartnership,
        "53" -> CharitableOrg,
        "54" -> RegSociety
      )

      validEntityTypes map {
        case (value, expected) =>
          val res = form.bind(Json.obj(LeadPartnerForm.leadPartnerEntityType -> value))
          res.get mustBe expected
      }
    }

    "fail to parse an invalid entity" in {
      val invalidEntityTypes = Seq(
        "",
        "Z2"
      )

      invalidEntityTypes map { value =>
        val res = form.bind(Json.obj(LeadPartnerForm.leadPartnerEntityType -> value))
        res.errors.head mustBe FormError(LeadPartnerForm.leadPartnerEntityType, Seq(LeadPartnerForm.leadPartnerError), Seq())
      }
    }
  }

}
