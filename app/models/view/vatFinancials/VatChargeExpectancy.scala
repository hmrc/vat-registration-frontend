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

package models.view.vatFinancials

import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class VatChargeExpectancy(yesNo: String)

object VatChargeExpectancy {

  val VAT_CHARGE_YES = "VAT_CHARGE_YES"
  val VAT_CHARGE_NO = "VAT_CHARGE_NO"

  val valid = (item: String) => List(VAT_CHARGE_YES, VAT_CHARGE_NO).contains(item.toUpperCase)

  implicit val format: OFormat[VatChargeExpectancy] = Json.format[VatChargeExpectancy]

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    vs.financials.map(_.reclaimVatOnMostReturns).collect {
      case true => VatChargeExpectancy(VAT_CHARGE_YES)
      case false => VatChargeExpectancy(VAT_CHARGE_NO)
    }
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: VatChargeExpectancy, g: VatFinancials) =>
    g.copy(reclaimVatOnMostReturns = c.yesNo == VAT_CHARGE_YES)
  }

}
