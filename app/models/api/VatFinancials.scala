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

package models.api

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class VatFinancials(bankAccount: Option[VatBankAccount] = None,
                         turnoverEstimate: Long,
                         zeroRatedSalesEstimate: Option[Long] = None,
                         reclaimVatOnMostReturns: Boolean,
                         vatAccountingPeriod: VatAccountingPeriod
                        )

object VatFinancials {

  implicit val format: OFormat[VatFinancials] = (
    (__ \ "bankAccount").formatNullable[VatBankAccount] and
      (__ \ "turnoverEstimate").format[Long] and
      (__ \ "zeroRatedTurnoverEstimate").formatNullable[Long] and
      (__ \ "reclaimVatOnMostReturns").format[Boolean] and
      (__ \ "accountingPeriods").format[VatAccountingPeriod]
    ) (VatFinancials.apply, unlift(VatFinancials.unapply))

  val default = VatFinancials(turnoverEstimate = 0L, vatAccountingPeriod = VatAccountingPeriod.default, reclaimVatOnMostReturns = false)

}
