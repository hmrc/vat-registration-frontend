/*
 * Copyright 2026 HM Revenue & Customs
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
import models.bars.BankAccountType
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints.maxLength

object HasCompanyBankAccountForm extends RequiredBooleanForm {

  override val errorMsg                      = "validation.hasCompanyBankAccount.missing"
  val HAS_COMPANY_BANK_ACCOUNT_RADIO: String = "value"

  val form = Form(
    single(HAS_COMPANY_BANK_ACCOUNT_RADIO -> requiredBoolean)
  )
}

object ChooseAccountTypeForm {

  val BUSINESS_OR_PERSONAL_ACCOUNT_RADIO: String = "value"
  val errorMsg                                   = "validation.chooseAccountType.missing"

  val form: Form[BankAccountType] = Form(
    single(
      BUSINESS_OR_PERSONAL_ACCOUNT_RADIO -> default(text, "").verifying(stopOnFail(
        mandatory(errorMsg)
      )).transform[BankAccountType](
        s => BankAccountType.fromString(s).get,
        _.asBars
      )
    )
  )
}

object EnterBankAccountDetailsForm {

  val ACCOUNT_NAME   = "accountName"
  val ACCOUNT_NUMBER = "accountNumber"
  val SORT_CODE      = "sortCode"
  val ROLL_NUMBER    = "rollNumber"

  val accountNameEmptyKey     = "validation.companyBankAccount.name.missing"
  val accountNameMaxLengthKey = "validation.companyBankAccount.name.maxLength"
  val accountNameInvalidKey   = "validation.companyBankAccount.name.invalid"
  val accountNumberEmptyKey   = "validation.companyBankAccount.number.missing"
  val accountNumberInvalidKey = "validation.companyBankAccount.number.invalid"
  val sortCodeEmptyKey        = "validation.companyBankAccount.sortCode.missing"
  val sortCodeInvalidKey      = "validation.companyBankAccount.sortCode.invalid"
  val rollNumberInvalidKey    = "validation.companyBankAccount.rollNumber.invalid"

  val invalidAccountReputationKey     = "sortCodeAndAccountGroup"
  val invalidAccountReputationMessage = "validation.companyBankAccount.invalidCombination"

  private val accountNameRegex     = """^[A-Za-z0-9 '’‘()\[\]{}<>!«»"ʺ˝ˮ?/\\+=%#*&$€£_\-@¥.,:;]{1,60}$""".r
  private val accountNameMaxLength = 60
  private val accountNumberRegex   = """[0-9]{6,8}""".r
  private val sortCodeRegex        = """[0-9]{6}""".r
  private val rollNumberMaxLength  = 25

  val form = Form[BankAccountDetails](
    mapping(
      ACCOUNT_NAME -> text.verifying(
        stopOnFail(
          mandatory(accountNameEmptyKey),
          maxLength(accountNameMaxLength, accountNameMaxLengthKey),
          matchesRegex(accountNameRegex, accountNameInvalidKey)
        )),
      ACCOUNT_NUMBER -> text
        .transform(removeSpaces, identity[String])
        .verifying(
          stopOnFail(
            mandatory(accountNumberEmptyKey),
            matchesRegex(accountNumberRegex, accountNumberInvalidKey)
          )),
      SORT_CODE -> text
        .transform(removeSpaces, identity[String])
        .verifying(
          stopOnFail(
            mandatory(sortCodeEmptyKey),
            matchesRegex(sortCodeRegex, sortCodeInvalidKey)
          )),
      ROLL_NUMBER -> optional(
        text
          .transform(removeSpaces, identity[String])
          .verifying(
            maxLength(rollNumberMaxLength, rollNumberInvalidKey)
          )
      )
    )((accountName, accountNumber, sortCode, rollNumber) => BankAccountDetails.apply(accountName, accountNumber, sortCode, rollNumber))(
      bankAccountDetails =>
        BankAccountDetails.unapply(bankAccountDetails).map { case (accountName, accountNumber, sortCode, rollNumber, _) =>
          (accountName, accountNumber, sortCode, rollNumber)
        })
  )

  val formWithInvalidAccountReputation: Form[BankAccountDetails] =
    form.withError(invalidAccountReputationKey, invalidAccountReputationMessage)

}
