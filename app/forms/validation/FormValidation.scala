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

import java.time.LocalDate

import cats.Show
import forms.vatDetails.vatFinancials.SortCode
import models.DateModel
import org.apache.commons.lang3.StringUtils
import play.api.Logger
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

  def mandatoryText(errorSubCode: String): Constraint[String] = Constraint { input: String =>
    if (StringUtils.isNotBlank(input)) Valid else Invalid(s"validation.$errorSubCode.missing")
  }

  def mandatoryNumericText(errorSubCode: String): Constraint[String] = Constraint {
    val NumericText = """[0-9]+""".r
    (input: String) =>
      input match {
        case NumericText(_*) => Valid
        case _ if StringUtils.isBlank(input) => Invalid(s"validation.$errorSubCode.missing")
        case _ => Invalid("validation.numeric")
      }
  }


  private def unconstrained[T] = Constraint[T] { (t: T) => Valid }

  def inRange[T](minValue: T, maxValue: T, errorSubCode: String = "")(implicit ordering: Ordering[T]): Constraint[T] =
    Constraint[T] { (t: T) =>
      Logger.info(s"Checking constraint for value $t in the range of [$minValue, $maxValue]")
      (ordering.compare(t, minValue).signum, ordering.compare(t, maxValue).signum) match {
        case (1, -1) | (0, _) | (_, 0) => Valid
        case (_, 1) => Invalid(ValidationError(s"validation.$errorSubCode.range.above", maxValue))
        case (-1, _) => Invalid(ValidationError(s"validation.$errorSubCode.range.below", minValue))
      }
    }

  val taxEstimateTextToLong = textToLong(0, 1000000000000000L) _
  val numberOfWorkersToInt = textToInt(1, 99999) _

  private def textToInt(min: Int, max: Int)(s: String): Int = {
    // assumes input string will be numeric
    val bigInt = BigInt(s)
    bigInt match {
      case _ if bigInt < min => Int.MinValue
      case _ if bigInt > max => Int.MaxValue
      case _ => bigInt.toInt
    }
  }

  private def textToLong(min: Long, max: Long)(s: String): Long = {
    // assumes input string will be numeric
    val bigInt = BigInt(s)
    bigInt match {
      case _ if bigInt < min => Long.MinValue
      case _ if bigInt > max => Long.MaxValue
      case _ => bigInt.toLong
    }
  }

  def intToText(i: Int): String = i.toString
  def longToText(l: Long): String = l.toString

  def boundedLong(errorSubCode: String): Constraint[Long] = Constraint {
    input: Long =>
      input match {
        case Long.MaxValue => Invalid(s"validation.$errorSubCode.high")
        case Long.MinValue => Invalid(s"validation.$errorSubCode.low")
        case _ => Valid
      }
  }

  def boundedInt(errorSubCode: String): Constraint[Int] = Constraint {
    input: Int =>
      input match {
        case Int.MaxValue => Invalid(s"validation.$errorSubCode.high")
        case Int.MinValue => Invalid(s"validation.$errorSubCode.low")
        case _ => Valid
      }
  }

  def nonEmptyValidText(errorSubCode: String, Pattern: Regex): Constraint[String] = Constraint[String] {
    input: String =>
      input match {
        case Pattern(_*) => Valid
        case s if StringUtils.isNotBlank(s) => Invalid(s"validation.$errorSubCode.invalid")
        case _ => Invalid(s"validation.$errorSubCode.empty")
      }
  }

  /* overrides Play's implicit stringFormatter and handles missing options (e.g. no radio button selected) */
  private def stringFormat(errorSubCode: String): Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) = data.get(key).toRight(Seq(FormError(key, s"validation.$errorSubCode.option.missing", Nil)))

    def unbind(key: String, value: String) = Map(key -> value)
  }

  def missingFieldMapping(errorSubCode: String): Mapping[String] = FieldMapping[String]()(stringFormat(errorSubCode))

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

  object Dates {

    def nonEmptyDateModel(errorSubCode: String): Constraint[DateModel] =
      Constraint(dm => mandatoryText(errorSubCode)(Seq(dm.day, dm.month, dm.day).mkString.trim))

    def validDateModel(dateConstraint: Constraint[LocalDate] = unconstrained, errorSubCode: => String): Constraint[DateModel] =
      Constraint(dm => dm.toLocalDate.fold[ValidationResult](Invalid(s"validation.$errorSubCode.invalid"))(dateConstraint(_)))

  }

}