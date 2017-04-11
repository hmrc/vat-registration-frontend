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

package forms.vatFinancials.vatAccountingPeriod

import forms.FormValidation._
import models.view.vatFinancials.vatAccountingPeriod.AccountingPeriod
import play.api.data.Form
import play.api.data.Forms._

object AccountingPeriodForm {
  val RADIO_ACCOUNTING_PERIOD: String = "accountingPeriodRadio"

  val form = Form(
    mapping(
      RADIO_ACCOUNTING_PERIOD -> missingFieldMapping()("accounting.period").verifying(AccountingPeriod.valid)
    )(AccountingPeriod.apply)(AccountingPeriod.unapply)
  )
}
