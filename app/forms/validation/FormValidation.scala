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
import forms.vatDetails.vatFinancials.SortCode
import org.apache.commons.lang3.StringUtils
import play.api.data.format.Formatter
import play.api.data.validation._
import play.api.data.{FieldMapping, FormError, Mapping}

import scala.util.matching.Regex

private[forms] object FormValidation {

  def regexPattern(pattern: Regex, errorSubCode: String, mandatory: Boolean = true): Constraint[String] = Constraint {
    input: String =>
      mandatoryText(errorSubCode)(input) match {
        case Valid => Constraints.pattern(pattern, error = s"validation.$errorSubCode.invalid")(input)
        case err => if (mandatory) err else Valid
      }
  }

  def mandatoryText(specificCode: String): Constraint[String] = Constraint {
    (input: String) => if (StringUtils.isNotBlank(input)) Valid else Invalid(s"validation.$specificCode.missing")
  }

  def mandatoryNumericText(specificCode: String): Constraint[String] = Constraint {
    val numericText = """[0-9]+"""
    (input: String) => input match {
      case _ if (StringUtils.isBlank(input)) => Invalid(s"validation.$specificCode.missing")
      case _ if ( ! input.matches(numericText)) => Invalid("validation.numeric")
      case _ => Valid
    }
  }

  val taxEstimateTextToLong = textToLong(0, 1000000000000000L) _

  private def textToLong(min: Long, max: Long)(s: String): Long = {
    // assumes input string will be numeric
    val bigInt = BigInt(s)
    bigInt match {
      case _ if bigInt < min => Long.MinValue
      case _ if bigInt > max => Long.MaxValue
      case _ => bigInt.toLong
    }
  }

  def longToText(l: Long): String = l.toString

  def boundedLong(specificCode: String): Constraint[Long] = Constraint {
    input: Long =>
      input match {
        case Long.MaxValue => Invalid(s"validation.$specificCode.high")
        case Long.MinValue => Invalid(s"validation.$specificCode.low")
        case _ => Valid
      }
  }

  def nonEmptyValidText(specificCode: String, Pattern: Regex): Constraint[String] = Constraint[String] {
    input: String =>
      input match {
        case Pattern(_*) => Valid
        case s if StringUtils.isNotBlank(s) => Invalid(s"validation.$specificCode.invalid")
        case _ => Invalid(s"validation.$specificCode.empty")
      }
  }

  /* overrides Play's implicit stringFormatter and handles missing options (e.g. no radio button selected) */
  private def stringFormat(specificCode: String): Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) = data.get(key).toRight(Seq(FormError(key, s"validation.$specificCode.option.missing", Nil)))

    def unbind(key: String, value: String) = Map(key -> value)
  }

  def missingFieldMapping(specificCode: String): Mapping[String] = FieldMapping[String]()(stringFormat(specificCode))

  object BankAccount {

    import cats.instances.string._

    private val SortCode = """^([0-9]{2})-([0-9]{2})-([0-9]{2})$""".r
    private val AccountName = """[A-Za-z0-9\-',/& ]{1,150}""".r
    private val AccountNumber = """[0-9]{8}""".r

    def accountName(errorSubstring: String, mandatory: Boolean = true): Constraint[String] =
      regexPattern(AccountName, s"$errorSubstring.name", mandatory)

    def accountNumber(errorSubstring: String, mandatory: Boolean = true): Constraint[String] =
      regexPattern(AccountNumber, s"$errorSubstring.number", mandatory)

    def accountSortCode(errorSubstring: String, mandatory: Boolean = true): Constraint[SortCode] = Constraint {
      (in: SortCode) =>
        regexPattern(SortCode, errorSubCode = s"$errorSubstring.sortCode", mandatory)(Show[SortCode].show(in))
    }

  }

}
