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

import models.api.{VatAccountingPeriod, VatFinancials, VatScheme}
import models.{ApiModelTransformer, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class AccountingPeriod(accountingPeriod: String) extends ViewModelTransformer[VatFinancials] {

  // Upserts (selectively converts) a View model object to its API model counterpart
  override def toApi(vatFinancials: VatFinancials): VatFinancials =
    vatFinancials.copy(vatAccountingPeriod = VatAccountingPeriod(Some(accountingPeriod.toLowerCase), "quarterly"))

}

object AccountingPeriod extends ApiModelTransformer[AccountingPeriod] {

  val JAN_APR_JUL_OCT = "JAN_APR_JUL_OCT"
  val FEB_MAY_AUG_NOV = "FEB_MAY_AUG_NOV"
  val MAR_JUN_SEP_DEC = "MAR_JUN_SEP_DEC"

  implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]

  // Returns a view model for a specific part of a given VatScheme API model
  override def apply(vatScheme: VatScheme): AccountingPeriod = {
    vatScheme.financials match {
      case Some(financials) => financials.vatAccountingPeriod.periodStart match {
        case Some("jan_apr_jul_oct") => AccountingPeriod(JAN_APR_JUL_OCT)
        case Some("feb_may_aug_nov") => AccountingPeriod(FEB_MAY_AUG_NOV)
        case Some("mar_jun_sep_dec") => AccountingPeriod(MAR_JUN_SEP_DEC)
        case _ => AccountingPeriod.empty
      }
      case _ => AccountingPeriod.empty
    }
  }

  def empty: AccountingPeriod = AccountingPeriod("")

}
