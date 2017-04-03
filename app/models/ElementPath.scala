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

import play.api.libs.json._

sealed trait ElementPath {
  val path: String
  val name: String
}

object ElementPath {

  implicit object ElementPathFormatter extends Format[ElementPath] {
    override def writes(e: ElementPath): JsValue = JsString(e.name)

    override def reads(json: JsValue): JsResult[ElementPath] = json.as[String] match {
      case VatBankAccountPath.name => JsSuccess(VatBankAccountPath)
      case ZeroRatedTurnoverEstimatePath.name => JsSuccess(ZeroRatedTurnoverEstimatePath)
      case AccountingPeriodStartPath.name => JsSuccess(AccountingPeriodStartPath)
      case _ => JsError("unrecognised element name")
    }
  }

}

case object VatBankAccountPath extends ElementPath {
  override val path = "financials.bankAccount"
  override val name = "vat-bank-account"
}

case object ZeroRatedTurnoverEstimatePath extends ElementPath {
  override val path = "financials.zeroRatedTurnoverEstimate"
  override val name = "zero-rated-turnover-estimate"
}

case object AccountingPeriodStartPath extends ElementPath {
  override val path = "financials.accountingPeriods.periodStart"
  override val name = "accounting-period-start"
}