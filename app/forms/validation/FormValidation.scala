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

import cats.Show
import forms.vatDetails.SortCode
import org.apache.commons.lang3.StringUtils
import play.api.data.validation.{Constraint, Constraints, Invalid, Valid}

import scala.util.matching.Regex

private[forms] object FormValidation {

  def patternCheckingConstraint[T: Show](pattern: Regex, errorSubCode: String, mandatory: Boolean): Constraint[T] = Constraint {
    input: T =>
      val s: String = Show[T].show(input)
      mandatoryText(errorSubCode)(s) match {
        case Valid => Constraints.pattern(pattern, error = s"validation.$errorSubCode.invalid")(s)
        case err => if (mandatory) err else Valid
      }
  }

  def mandatoryText(specificCode: String): Constraint[String] = Constraint {
    (input: String) => if (StringUtils.isNotBlank(input)) Valid else Invalid(s"validation.$specificCode.missing")
  }

  object BankAccount {

    import cats.instances.string._

    private val SortCode = """^([0-9]{2})-([0-9]{2})-([0-9]{2})$""".r
    private val AccountName = """[A-Za-z0-9\-',/& ]{1,150}""".r
    private val AccountNumber = """[0-9]{8}""".r


    def accountName(errorSubstring: String, mandatory: Boolean = true): Constraint[String] =
      patternCheckingConstraint(AccountName, s"$errorSubstring.name", mandatory)

    def accountNumber(errorSubstring: String, mandatory: Boolean = true): Constraint[String] =
      patternCheckingConstraint(AccountNumber, s"$errorSubstring.number", mandatory)

    def accountSortCode(errorSubstring: String, mandatory: Boolean = true): Constraint[SortCode] =
      patternCheckingConstraint(SortCode, errorSubCode = s"$errorSubstring.sortCode", mandatory)

  }

}
