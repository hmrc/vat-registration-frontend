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

import forms.FormValidation
import forms.FormValidation.ErrorCode
import forms.vatFinancials.vatBankAccount.SortCode
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

  "Bank account name validation" must {

    val constraint = FormValidation.BankAccount.accountName("account")

    "accept valid bank account name" in {
      forAll(Seq("Account Name", "Peter Perhac & Sons", "X"))(constraint(_) shouldBe Valid)
    }

    "reject blank bank account name" in {
      forAll(Seq("", " ", "    \t   "))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.name.missing"
      })
    }

    "reject bank account name containing illegal characters" in {
      forAll(Seq("Foo * Bar", "_foobar_", "$$$"))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.name.invalid"
      })
    }

    "reject too long bank account name" in {
      inside(constraint(("text" * 100).mkString)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.name.invalid"
      }
    }

  }

  "Bank account number validation" must {

    val constraint = FormValidation.BankAccount.accountNumber("account")

    "accept valid bank account number" in {
      forAll(Seq("12345678", "00000000", "09876543"))(constraint(_) shouldBe Valid)
    }

    "reject blank bank account number" in {
      forAll(Seq("", " ", "    \t   "))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.number.missing"
      })
    }

    "reject bank account number containing illegal characters" in {
      forAll(Seq("10000e30", "-2001234", "$mydollars$"))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.number.invalid"
      })
    }

    "reject too long bank account number" in {
      forAll(Seq(("1" * 20).mkString, "123456789"))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.number.invalid"
      })
    }

  }


  "Bank account sort code validation" must {

    val constraint = FormValidation.BankAccount.accountSortCode("account")

    "accept valid bank account sortCode" in {
      forAll(Seq(
        SortCode("99", "00", "01"),
        SortCode("00", "00", "00"),
        SortCode("99", "99", "99")))(constraint(_) shouldBe Valid)
    }

    "reject blank bank account sortCode" in {
      forAll(Seq(
        SortCode("", "", ""),
        SortCode(" ", " ", " "),
        SortCode("    \t   ", "    \t   ", "    \t   ")))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.sortCode.missing"
      })
    }

    "reject bank account sortCode containing illegal characters" in {
      forAll(Seq(
        SortCode("20", "$$", "45"),
        SortCode("ab", "cd", "ef"),
        SortCode("$#", "#", "@")))(in => inside(constraint(in)) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.sortCode.invalid"
      })
    }

    "reject too long bank account sortCode" in {
      inside(constraint(SortCode("eigewig", "eogbweougb", "oegweigb"))) {
        case Invalid(err :: _) => err.message shouldBe "validation.account.sortCode.invalid"
      }
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

  }

}