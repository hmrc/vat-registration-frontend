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

import java.time.LocalDate

import cats.Show
import models.{DateModel, MonthYearModel}
import org.apache.commons.lang3.StringUtils
import play.api.Logger
import play.api.data.format.Formatter
import play.api.data.validation._
import play.api.data.{FieldMapping, FormError, Mapping}
import StringUtils.isNotBlank

import scala.util.Try
import scala.util.matching.Regex

private[forms] object FormValidation {

  type ErrorCode = String

  def regexPattern(pattern: Regex, mandatory: Boolean = true)(implicit e: ErrorCode): Constraint[String] = Constraint {
    input: String =>
      mandatoryText.apply(input) match {
        case Valid => Constraints.pattern(pattern, error = s"validation.$e.invalid")(input)
        case err => if (mandatory) err else Valid
      }
  }

  def matchesRegex(pattern: Regex, errKey: String): Constraint[String] = Constraint {
    input: String => Constraints.pattern(pattern, error = errKey)(input)
  }

  def mandatory(errKey: String): Constraint[String] = Constraint { input: String =>
    if (StringUtils.isNotBlank(input)) Valid else Invalid(errKey)
  }

  def mandatoryTuple3(errKey: String): Constraint[(String, String, String)] = Constraint { input: (String, String, String) =>
    input match {
      case (_1, _2, _3) if isNotBlank(_1) & isNotBlank(_2) & isNotBlank(_3) => Valid
      case _ => Invalid(errKey)
    }
  }

  def mandatoryText()(implicit e: ErrorCode): Constraint[String] = Constraint { input: String =>
    if (StringUtils.isNotBlank(input)) Valid else Invalid(s"validation.$e.missing")
  }

  def maxLenText(maxlen: Integer)(implicit e: ErrorCode): Constraint[String] = Constraint { input: String =>
    if (StringUtils.length(input) > maxlen) Invalid(s"validation.$e.maxlen") else Valid
  }

  def mandatoryNumericText()(implicit e: ErrorCode): Constraint[String] = Constraint {
    val NumericText = """[0-9]+""".r
    (input: String) =>
      input match {
        case NumericText(_*) => Valid
        case _ if StringUtils.isBlank(input) => Invalid(s"validation.$e.missing")
        case _ => Invalid("validation.numeric")
      }
  }

  private def unconstrained[T] = Constraint[T] { (t: T) => Valid }

  def inRange[T](minValue: T, maxValue: T)(implicit ordering: Ordering[T], e: ErrorCode): Constraint[T]
      = inRangeWithArgs[T](minValue, maxValue)(Seq())(ordering, e)

  def inRangeWithArgs[T](minValue: T, maxValue: T)(args: Seq[Any] = Seq())(implicit ordering: Ordering[T], e: ErrorCode): Constraint[T] =
    Constraint[T] { (t: T) =>
      Logger.info(s"Checking constraint for value $t in the range of [$minValue, $maxValue]")
      (ordering.compare(t, minValue).signum, ordering.compare(t, maxValue).signum) match {
        case (1, -1) | (0, _) | (_, 0) => Valid
        case (_, 1) => Invalid(ValidationError(s"validation.$e.range.above", maxValue))
        case (-1, _) if !args.isEmpty  => Invalid(ValidationError(s"validation.$e.range.below", args.head))
        case (-1, _) => Invalid(ValidationError(s"validation.$e.range.below", minValue))
      }
    }

  def onOrAfter[T](minValue: T)(implicit ordering: Ordering[T], e: ErrorCode): Constraint[T] =
    Constraint[T] { (t: T) =>
      Logger.info(s"Checking constraint for value $t on or after [$minValue]")
      (ordering.compare(t, minValue).signum) match {
        case (1) | (0) => Valid
        case (-1) => Invalid(ValidationError(s"validation.$e.range.below", minValue))
      }
    }

  val taxEstimateTextToLong = textToLong(0, 1000000000000000L) _
  val numberOfWorkersToInt = textToInt(1, 99999) _

  def removeSpaces(text: String) = StringUtils.remove(text, ' ')
  def removeNewlineAndTrim(s: String): String = s.replaceAll("\r\n|\r|\n|\t", " ").trim

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

  def boundedLong()(implicit e: ErrorCode): Constraint[Long] = Constraint {
    input: Long =>
      input match {
        case Long.MaxValue => Invalid(s"validation.$e.high")
        case Long.MinValue => Invalid(s"validation.$e.low")
        case _ => Valid
      }
  }

  def boundedInt()(implicit e: ErrorCode): Constraint[Int] = Constraint {
    input: Int =>
      input match {
        case Int.MaxValue => Invalid(s"validation.$e.high")
        case Int.MinValue => Invalid(s"validation.$e.low")
        case _ => Valid
      }
  }

  def nonEmptyValidText(Pattern: Regex)(implicit e: ErrorCode): Constraint[String] = Constraint[String] {
    input: String =>
      input match {
        case Pattern(_*) => Valid
        case s if StringUtils.isNotBlank(s) => Invalid(s"validation.$e.invalid")
        case _ => Invalid(s"validation.$e.missing")
      }
  }


  /* overrides Play's implicit stringFormatter and handles missing options (e.g. no radio button selected) */
  private def stringFormat(suffix: String)(args: Seq[Any] = Seq())(implicit e: ErrorCode): Formatter[String] = new Formatter[String] {

    def bind(key: String, data: Map[String, String]) = data.get(key).toRight(
      Seq(FormError(key, s"validation.$e.$suffix", args))
    )

    def unbind(key: String, value: String) = Map(key -> value)
  }

  private def booleanFormat()(args: Seq[Any] = Seq())(implicit e: ErrorCode): Formatter[Boolean] = new Formatter[Boolean] {
    def bind(key: String, data: Map[String, String]) = data.get(key).flatMap(input => Try(input.toBoolean).toOption)
      .toRight(Seq(FormError(key, s"validation.$e.missing", args)))

    def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  def textMapping()(implicit e: ErrorCode): Mapping[String] = FieldMapping[String]()(stringFormat("missing")(Seq()))

  def textMappingWithMessageArgs()(args: Seq[Any] = Seq())(implicit e: ErrorCode): Mapping[String] = FieldMapping[String]()(stringFormat("missing")(args))
  def missingBooleanFieldMappingArgs()(args: Seq[Any] = Seq())(implicit e: ErrorCode): Mapping[Boolean] = FieldMapping[Boolean]()(booleanFormat()(args))

  def missingBooleanFieldMapping()(implicit e: ErrorCode): Mapping[Boolean] =
    FieldMapping[Boolean]()(booleanFormat()(Seq()))

  object Dates {

    def nonEmptyDateModel(constraint: => Constraint[DateModel] = unconstrained)(implicit e: ErrorCode): Constraint[DateModel] =
      Constraint { dm =>
        mandatoryText.apply(Seq(dm.day, dm.month, dm.day).mkString.trim) match {
          case Valid => constraint(dm)
          case err@_ => err
        }
      }

    def nonEmptyMonthYearModel(constraint: => Constraint[MonthYearModel] = unconstrained)(implicit e: ErrorCode): Constraint[MonthYearModel] =
      Constraint { pdm =>
        mandatoryText.apply(Seq(pdm.month, pdm.year).mkString.trim) match {
          case Valid => constraint(pdm)
          case err@_ => err
        }
      }

    def validDateModel(dateConstraint: => Constraint[LocalDate] = unconstrained)(implicit e: ErrorCode): Constraint[DateModel] =
      Constraint(dm => dm.toLocalDate.fold[ValidationResult](Invalid(s"validation.$e.invalid"))(dateConstraint(_)))

    def validPartialMonthYearModel(dateConstraint: => Constraint[LocalDate] = unconstrained)(implicit e: ErrorCode): Constraint[MonthYearModel] =
      Constraint(dm => dm.toLocalDate.fold[ValidationResult](Invalid(s"validation.$e.invalid"))(dateConstraint(_)))

  }

}
