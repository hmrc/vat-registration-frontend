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

package forms

import features.financials.models.Stagger
import features.financials.models.Stagger._
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import forms.FormValidation._

object AccountingPeriodForm {

  val accountingPeriodEmptyKey = "validation.accounting.period.missing"
  val accountingPeriodInvalidKey = "validation.accounting.period.missing"
  val ACCOUNTING_PERIOD: String = "accountingPeriodRadio"

  val form = Form(
    single(ACCOUNTING_PERIOD -> text.verifying(StopOnFirstFail(
      mandatory(accountingPeriodEmptyKey),
      matches(List(jan, feb, mar), accountingPeriodInvalidKey)
    )).transform(Stagger.withName, (s:Stagger.Value) => s.toString))
  )
}
