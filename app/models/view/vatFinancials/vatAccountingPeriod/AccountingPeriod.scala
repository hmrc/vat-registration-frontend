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

package models.view.vatFinancials.vatAccountingPeriod

import models.api.{VatFinancials, VatScheme}
import models.{ApiModelTransformer, S4LVatFinancials, ViewModelFormat, ViewModelTransformer}
import play.api.libs.json.{Json, OFormat}

case class AccountingPeriod(accountingPeriod: String)

object AccountingPeriod {

  val JAN_APR_JUL_OCT = "JAN_APR_JUL_OCT"
  val FEB_MAY_AUG_NOV = "FEB_MAY_AUG_NOV"
  val MAR_JUN_SEP_DEC = "MAR_JUN_SEP_DEC"

  val valid = (item: String) => List(JAN_APR_JUL_OCT, FEB_MAY_AUG_NOV, MAR_JUN_SEP_DEC).contains(item.toUpperCase)

  implicit val format: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]

  implicit val viewModelFormat = ViewModelFormat(
    readF = (group: S4LVatFinancials) => group.accountingPeriod,
    updateF = (c: AccountingPeriod, g: Option[S4LVatFinancials]) =>
      g.getOrElse(S4LVatFinancials()).copy(accountingPeriod = Some(c))
  )

  // Returns a view model for a specific part of a given VatScheme API model
  implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
    for {
      f <- vs.financials
      ps <- f.accountingPeriods.periodStart
    } yield AccountingPeriod(ps.toUpperCase())
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: AccountingPeriod, g: VatFinancials) =>
    g.copy(accountingPeriods = g.accountingPeriods.copy(periodStart = Some(c.accountingPeriod.toLowerCase)))
  }

}
