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

import forms.FormValidation._
import models.BankAccountDetails
import models.api.{BankAccountDetailsStatus, IndeterminateStatus}
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.play.mappers.StopOnFirstFail

import scala.util.matching.Regex

object HasCompanyBankAccountForm extends RequiredBooleanForm {

  override val errorMsg = "validation.company.bank.account.missing"
  val HAS_COMPANY_BANK_ACCOUNT_RADIO: String = "value"

  val form = Form(
    single(HAS_COMPANY_BANK_ACCOUNT_RADIO -> requiredBoolean)
  )
}

object EnterBankAccountDetailsForm {

  val ACCOUNT_NAME = "accountName"
  val ACCOUNT_NUMBER = "accountNumber"
  val SORT_CODE = "sortCode"

  val accountNameEmptyKey = "validation.companyBankAccount.name.missing"
  val accountNameInvalidKey = "validation.companyBankAccount.name.invalid"
  val accountNumberEmptyKey = "validation.companyBankAccount.number.missing"
  val accountNumberInvalidKey = "validation.companyBankAccount.number.invalid"
  val sortCodeEmptyKey = "validation.companyBankAccount.sortCode.missing"
  val sortCodeInvalidKey = "validation.companyBankAccount.sortCode.invalid"

  val invalidAccountReputationKey = "sortCodeAndAccountGroup"
  val invalidAccountReputationMessage = "validation.companyBankAccount.invalidCombination"

  private val accountNameRegex = """^[A-Za-z0-9\-',/& ]{1,150}$""".r
  private val accountNumberRegex = """[0-9]{8}""".r
  private val sortCodeRegex = """[0-9]{6}""".r

  val form = Form[BankAccountDetails] (
    mapping(
      ACCOUNT_NAME -> text.verifying(StopOnFirstFail(
        mandatory(accountNameEmptyKey),
        matchesRegex(accountNameRegex, accountNameInvalidKey)
      )),
      ACCOUNT_NUMBER -> text.verifying(StopOnFirstFail(
        mandatory(accountNumberEmptyKey),
        matchesRegex(accountNumberRegex, accountNumberInvalidKey)
      )),
      SORT_CODE -> text.verifying(StopOnFirstFail(
        mandatory(sortCodeEmptyKey),
        matchesRegex(sortCodeRegex, sortCodeInvalidKey)
      ))
    )
    ((accountName, accountNumber, sortCode) => BankAccountDetails.apply(accountName, accountNumber, sortCode, None))
    (bankAccountDetails =>
      BankAccountDetails.unapply(bankAccountDetails).map {
        case (accountName, accountNumber, sortCode, _) => (accountName, accountNumber, sortCode)
      }
    )
  )

  val formWithInvalidAccountReputation: Form[BankAccountDetails] =
    form.withError(invalidAccountReputationKey, invalidAccountReputationMessage)

}
