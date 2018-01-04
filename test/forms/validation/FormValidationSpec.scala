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

import forms.FormValidation
import forms.FormValidation.ErrorCode
import models.DateModel
import org.scalatest.{Inside, Inspectors}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.play.test.UnitSpec

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

}
