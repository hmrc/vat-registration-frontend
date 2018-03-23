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

package forms.validation

import java.time.LocalDate

import org.joda.time.{LocalDate => JodaLocalDate}
import forms.FormValidation
import forms.FormValidation.ErrorCode
import models.DateModel
import org.scalatest.{Inside, Inspectors}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.time.workingdays.{BankHoliday, BankHolidaySet}

class FormValidationSpec extends UnitSpec with Inside with Inspectors {

  "mandatoryText" must {

    val constraint = FormValidation.mandatoryText()("errorCode")

    "accept a non-blank string as Valid" in {
      constraint("non-blank string") shouldBe Valid
    }

    "reject blank string" in {
      forAll(Seq("", "  ", "    \t   "))(constraint(_) shouldBe Invalid("validation.errorCode.missing"))
    }
  }


  "mandatoryNumericText" must {

    val constraint = FormValidation.mandatoryNumericText()("errorCode")

    "accept a numeric string integer" in {
      constraint("1234") shouldBe Valid
    }

    "reject blank string" in {
      forAll(Seq("", "  ", "    \t   "))(constraint(_) shouldBe Invalid("validation.errorCode.missing"))
    }

    "reject non-numeric string" in {
      constraint("1e10") shouldBe Invalid("validation.numeric")
    }
  }


  "nonEmptyValidText" should {
    val regex = """^[A-Za-z]{1,10}$""".r

    "return valid when string matches regex" in {
      val constraint = FormValidation.nonEmptyValidText(regex)("fieldName")
      constraint("abcdef") shouldBe Valid
    }

    "return invalid when string does not match regex" in {
      val constraint = FormValidation.nonEmptyValidText(regex)("fieldName")
      constraint("a123") shouldBe Invalid("validation.fieldName.invalid")
    }

    "return invalid when string is empty" in {
      val constraint = FormValidation.nonEmptyValidText(regex)("fieldName")
      constraint("") shouldBe Invalid("validation.fieldName.missing")
    }
  }

  "maxLenText" must {
    val maxlen = 70
    val constraint = FormValidation.maxLenText(maxlen)("errorCode")

    "accept an empty string as Valid" in {
      constraint("") shouldBe Valid
    }

    "accept maxLen string as Valid" in {
      constraint("c" * maxlen) shouldBe Valid
    }

    "reject maxLen+1 string" in {
      constraint("c" * (maxlen + 1)) shouldBe Invalid("validation.errorCode.maxlen")
    }

    "reject long string" in {
      constraint("long " * maxlen) shouldBe Invalid("validation.errorCode.maxlen")
    }
  }

  "taxEstimateTextToLong" must {
    "return MinValue when input converts to value less than zero" in {
      FormValidation.taxEstimateTextToLong("-1") shouldBe Long.MinValue
    }

    "return MaxValue when input converts to value greater than 1e15" in {
      FormValidation.taxEstimateTextToLong("100000000000000000") shouldBe Long.MaxValue
    }

    "return value when input converts to value between 0 and 1e15" in {
      FormValidation.taxEstimateTextToLong("1000") shouldBe 1000
    }
  }

  "removeSpaces" must {
    "leave an empty string unchanged " in {
      FormValidation.removeSpaces("") shouldBe ""
    }

    "remove all spaces from a non-empty string" in {
      FormValidation.removeSpaces("  a  b  c  ") shouldBe "abc"
    }
  }

  "removeNewlineAndTrim" must {
    "leave an empty string unchanged " in {
      FormValidation.removeSpaces("") shouldBe ""
    }

    "replace all newlines and tabs with a space from a non-empty string" in {
      FormValidation.removeNewlineAndTrim(" \na\nb\tc  ") shouldBe "a b c"
    }
  }

  "numberOfWorkersTextToInt" must {
    "return MinValue when input converts to value less than zero" in {
      FormValidation.numberOfWorkersToInt("-1") shouldBe Int.MinValue
    }

    "return MaxValue when input converts to value greater than 99999" in {
      FormValidation.numberOfWorkersToInt("100000") shouldBe Int.MaxValue
    }

    "return value when input converts to value between 1 and 99999" in {
      FormValidation.numberOfWorkersToInt("5") shouldBe 5
    }
  }

  "boundedLong constraint" must {
    val specificCode = "specific.code"
    val boundedLongConstraint = FormValidation.boundedLong()(specificCode)

    "return Invalid-low if input is Long.MinValue" in {
      inside(boundedLongConstraint(Long.MinValue)) {
        case Invalid(err :: _) => err.message shouldBe s"validation.$specificCode.low"
      }
    }

    "return Invalid-high if input is Long.MaxValue" in {
      inside(boundedLongConstraint(Long.MaxValue)) {
        case Invalid(err :: _) => err.message shouldBe s"validation.$specificCode.high"
      }
    }

    "return Valid if input is not Long.MinValue or Long.MaxValue" in {
      boundedLongConstraint(100) shouldBe Valid
    }

  }

    "boundedInt constraint" must {
    val specificCode = "specific.code"
    val boundedIntConstraint = FormValidation.boundedInt()(specificCode)

    "return Invalid-low if input is Int.MinValue" in {
      inside(boundedIntConstraint(Int.MinValue)) {
        case Invalid(err :: _) => err.message shouldBe s"validation.$specificCode.low"
      }
    }

    "return Invalid-high if input is Int.MaxValue" in {
      inside(boundedIntConstraint(Int.MaxValue)) {
        case Invalid(err :: _) => err.message shouldBe s"validation.$specificCode.high"
      }
    }

    "return Valid if input is not Int.MinValue or Int.MaxValue" in {
      boundedIntConstraint(100) shouldBe Valid
    }

  }

  "patternCheckingConstraint" must {

    val constraintMandatory = FormValidation.regexPattern("""[a-z]{3}""".r, mandatory = true)("errorCode")
    val constraintNonMandatory = FormValidation.regexPattern("""[a-z]{3}""".r, mandatory = false)("errorCode")

    "accept a string matching regex pattern as Valid" in {
      constraintMandatory.apply("abc") shouldBe Valid
    }

    "reject a string not matching regex pattern" in {
      inside(constraintMandatory.apply("ABC")) {
        case Invalid(err :: _) => err.message shouldBe "validation.errorCode.invalid"
      }
    }

    "reject an empty string if mandatory" in {
      inside(constraintMandatory.apply("")) {
        case Invalid(err :: _) => err.message shouldBe "validation.errorCode.missing"
      }
    }

    "accept an empty string if not mandatory" in {
      constraintNonMandatory.apply("") shouldBe Valid
    }

  }

  "Date validation" must {

    "accept valid date" in {
      val constraint: Constraint[DateModel] = FormValidation.Dates.validDateModel()("date")

      forAll(Seq(
        DateModel("1", "1", "1970"),
        DateModel("31", "12", "2100"),
        DateModel("29", "2", "2016"),
        DateModel("21", "3", "2017")
      ))(constraint(_) shouldBe Valid)
    }

    "reject invalid date" in {
      val constraint: Constraint[DateModel] = FormValidation.Dates.validDateModel()("date")

      forAll(Seq(
        DateModel("1", "1", "0"),
        DateModel("foo", "bar", ""),
        DateModel("29", "2", "2017"),
        DateModel("32", "3", "2017")
      ))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.date.invalid"
      })
    }

    "reject empty date" in {
      val constraint: Constraint[DateModel] = FormValidation.Dates.nonEmptyDateModel()("date")

      forAll(Seq(
        DateModel("  ", " ", ""),
        DateModel("   ", "   ", "   "),
        DateModel("", "", "")
      ))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.date.missing"
      })
    }

  }


  "Range validation" must {

    implicit val e:ErrorCode = "test"
    val constraint = FormValidation.inRange[Int](0, 100)
    val constraintWithErrorArgs = FormValidation.inRangeWithArgs[Int](0, 100)("Date Error")


    "accept values in range" in {
      forAll(Seq(0, 1, 2, 3, 50, 98, 99, 100))(constraint(_) shouldBe Valid)
    }

    "reject values below acceptable minimum" in {
      forAll(Seq(Int.MinValue, -10000, -1))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.test.range.below"
      })
    }

    "reject values above acceptable maximum" in {
      forAll(Seq(Int.MaxValue, 10000, 101))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.test.range.above"
      })
    }

    "reject values below acceptable minimum with custom error message" in {
      forAll(Seq(Int.MinValue, -10000, -1))(in => inside(constraintWithErrorArgs(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.test.range.below"
      })
    }

  }
  "isEmail" must {
    implicit val e:ErrorCode = "test"
    val constraint = FormValidation.IsEmail
    "validate a valid email" in {
      constraint.apply("foo@foo.com") shouldBe Valid
    }
    "invalidate an invalid email" in {
      constraint.apply("foo@foo.com;") shouldBe Invalid("validation.test.invalid")
    }
    "invalidate a blank email" in {
      constraint.apply("") shouldBe Invalid("validation.test.invalid")
    }
    "invalidate a email with no structure of an email" in {
      constraint.apply("fooBAR78&./,/.s$%£&^*%$£'a;") shouldBe Invalid("validation.test.invalid")
    }
  }

  "onOrAfter validation" must {

    implicit val e:ErrorCode = "test"
    val constraint = FormValidation.onOrAfter[Int](0)

    "accept values above the minimum" in {
      forAll(Seq(0, 1, 2, 3, 50, 98, 99, 100))(constraint(_) shouldBe Valid)
    }

    "reject values below the minimum" in {
      forAll(Seq(Int.MinValue, -1))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.test.range.below"
      })
    }

  }

  "matches" should {
    val constraint = FormValidation.matches(List("item1", "item2"), "testErrMsg")

    "return Valid if the input is contained within the list" in {
      constraint("item1") shouldBe Valid
    }

    "return Invalid with the provided error message if the input is not contained within the list" in {
      constraint("item3") shouldBe Invalid(ValidationError("testErrMsg"))
    }
  }

  "nonEmptyDate" should {
    val constraint = FormValidation.nonEmptyDate("testErrMsg")

    "return Valid if the tuple of strings provided has a day, month and year" in {
      constraint("day", "month", "year") shouldBe Valid
    }

    "return Invalid if the day in the tuple is missing" in {
      constraint("", "month", "year") shouldBe Invalid(ValidationError("testErrMsg"))
    }

    "return Invalid if the month in the tuple is missing" in {
      constraint("day", "", "year") shouldBe Invalid(ValidationError("testErrMsg"))
    }

    "return Invalid if the year in the tuple is missing" in {
      constraint("day", "month", "") shouldBe Invalid(ValidationError("testErrMsg"))
    }
  }

  "validDate" should {
    val constraint = FormValidation.validDate("testErrMsg")

    "return Valid if the tuple of strings provided is a valid date" in {
      constraint("30", "12", "2018") shouldBe Valid
    }

    "return Invalid if the provided dates day value is not a valid day" in {
      constraint("50", "1", "2018") shouldBe Invalid(ValidationError("testErrMsg"))
    }

    "return Invalid if the provided dates month value is not a valid month" in {
      constraint("1", "20", "2018") shouldBe Invalid(ValidationError("testErrMsg"))
    }

    "return Invalid if the provided dates year value is not a valid year" in {
      constraint("1", "1", "zOlB") shouldBe Invalid(ValidationError("testErrMsg"))
    }
  }

  "withinRange" should {
    val minDate = LocalDate.of(2018, 1, 1)
    val maxDate = LocalDate.of(2018, 12, 31)
    val constraint = FormValidation.withinRange(minDate, maxDate, "beforeMinErrMsg", "afterMaxErrMsg", List(minDate.toString, maxDate.toString))

    "return Valid if the date is after the min date and before the max date" in {
      constraint("15", "6", "2018") shouldBe Valid
    }

    "return Valid if the date is the same as the minimum date" in {
      constraint("1", "1", "2018") shouldBe Valid
    }

    "return Valid if the date is the same as the maximum date" in {
      constraint("31", "12", "2018") shouldBe Valid
    }

    "return Invalid if the date is before the minumum date with the correct error message" in {
      constraint("31", "12", "2017") shouldBe Invalid(ValidationError("beforeMinErrMsg", minDate.toString, maxDate.toString))
    }

    "return Invalid if the date is after the maximum date with the correct error message" in {
      constraint("1", "1", "2019") shouldBe Invalid(ValidationError("afterMaxErrMsg", minDate.toString, maxDate.toString))
    }
  }

  "withinFourYearsPast" should {
    val constraint = FormValidation.withinFourYearsPast("testErrMsg")
    val lessThan4 = LocalDate.now().minusYears(2)
    val exactly4 = LocalDate.now().minusYears(4)
    val moreThan4 = LocalDate.now().minusYears(6)

    "return Valid if the date is less than 4 years ago" in {
      constraint(s"${lessThan4.getDayOfMonth}", s"${lessThan4.getMonthValue}", s"${lessThan4.getYear}") shouldBe Valid
    }

    "return Valid if the date is exactly 4 years ago" in {
      constraint(s"${exactly4.getDayOfMonth}", s"${exactly4.getMonthValue}", s"${exactly4.getYear}") shouldBe Valid
    }

    "return Invalid if the date is more than 4 years ago" in {
      constraint(s"${moreThan4.getDayOfMonth}", s"${moreThan4.getMonthValue}", s"${moreThan4.getYear}") shouldBe Invalid(ValidationError("testErrMsg"))
    }
  }
}
