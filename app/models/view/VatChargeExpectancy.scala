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

import models.{ApiModelTransformer, ViewModelTransformer}
import models.api.{VatFinancials, VatScheme}
import play.api.libs.json.{Json, OFormat}

case class VatChargeExpectancy(yesNo: String) extends ViewModelTransformer[VatFinancials] {

  // Upserts (selectively converts) a View model object to its API model counterpart
  override def toApi(vatFinancials: VatFinancials): VatFinancials =
    vatFinancials.copy(reclaimVatOnMostReturns = toBoolean)

  def toBoolean: Boolean = {
    yesNo match {
      case VatChargeExpectancy.VAT_CHARGE_YES => true
      case VatChargeExpectancy.VAT_CHARGE_NO => false
    }
  }

}

object VatChargeExpectancy extends ApiModelTransformer[VatChargeExpectancy] {

  val VAT_CHARGE_YES = "VAT_CHARGE_YES"
  val VAT_CHARGE_NO = "VAT_CHARGE_NO"

  implicit val format: OFormat[VatChargeExpectancy] = Json.format[VatChargeExpectancy]

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): VatChargeExpectancy = {
    vatScheme.financials match {
      case Some(financials) => financials.reclaimVatOnMostReturns match {
        case true => VatChargeExpectancy(VAT_CHARGE_YES)
        case false => VatChargeExpectancy(VAT_CHARGE_NO)
      }
      case _ => VatChargeExpectancy.empty
    }
  }

  def empty: VatChargeExpectancy = VatChargeExpectancy("")

}
