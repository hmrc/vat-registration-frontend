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

package forms.validation

import forms.vatDetails.SortCode
import play.api.data.validation.{Constraint, Constraints}

object FormValidation {

  object BankAccount {

    private val SortCode = """^([0-9]{2})-([0-9]{2})-([0-9]{2})$""".r
    private val AccountName = """[A-Za-z0-9\-',/& ]{1,150}""".r
    private val AccountNumber = """[0-9]{8}""".r

    def accountNameConstraint(accountType: String): Constraint[String] =
      Constraints.pattern(AccountName, error = s"validation.$accountType.bankAccount.name.invalid")


    def accountNumberConstraint(accountType: String): Constraint[String] =
      Constraints.pattern(AccountNumber, error = s"validation.$accountType.bankAccount.number.invalid")

    def sortCodeConstraint(accountType: String): Constraint[SortCode] = Constraint { sortCode: SortCode =>
      Constraints.pattern(SortCode, error = s"validation.$accountType.bankAccount.sortCode.invalid")(sortCode.toString)
    }

  }

}
