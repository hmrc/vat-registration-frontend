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

import forms.FormValidation.{mandatory, matchesRegex}
import models.OverseasBankDetails
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

object OverseasBankAccountForm {

  val ACCOUNT_NAME = "accountName"
  val BIC = "BIC"
  val IBAN = "IBAN"

  val accountNameEmptyKey = "validation.overseasBankAccount.name.missing"
  val accountNameInvalidKey = "validation.overseasBankAccount.name.invalid"
  val bicEmptyKey = "validation.overseasBankAccount.bic.missing"
  val bicInvalidKey = "validation.overseasBankAccount.bic.invalid"
  val ibanEmptyKey = "validation.overseasBankAccount.iban.missing"
  val ibanInvalidKey = "validation.overseasBankAccount.iban.invalid"

  private val accountNameRegex = """^[A-Za-z0-9\-',/& ]{1,150}$""".r
  private val bicRegex = """^[A-Za-z0-9]{1,11}$""".r
  private val ibanRegex = """^[A-Za-z0-9]{1,34}$""".r

  val form: Form[OverseasBankDetails] = Form(
    mapping(
      ACCOUNT_NAME -> text.verifying(StopOnFirstFail(
        mandatory(accountNameEmptyKey),
        matchesRegex(accountNameRegex, accountNameInvalidKey)
      )),
      BIC -> text.verifying(StopOnFirstFail(
        mandatory(bicEmptyKey),
        matchesRegex(bicRegex, bicInvalidKey)
      )),
      IBAN -> text.verifying(StopOnFirstFail(
        mandatory(ibanEmptyKey),
        matchesRegex(ibanRegex, ibanInvalidKey)
      ))
    )(OverseasBankDetails.apply)(OverseasBankDetails.unapply)
  )

}
