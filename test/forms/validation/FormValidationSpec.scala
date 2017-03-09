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
import org.scalatest.{Inside, Inspectors}
import play.api.data.validation.{Invalid, Valid}
import uk.gov.hmrc.play.test.UnitSpec


class FormValidationSpec extends UnitSpec with Inside with Inspectors {

  import cats.instances.string._

  "mandatoryText" must {

    val constraint = FormValidation.mandatoryText("errorCode")

    "accept a non-blank string as Valid" in {
      constraint("non-blank string") shouldBe Valid
    }

    "reject null string" in {
      constraint(null) shouldBe Invalid("validation.errorCode.empty")
    }

    "reject empty string" in {
      constraint("") shouldBe Invalid("validation.errorCode.empty")
    }

    "reject blank string" in {
      constraint("    ") shouldBe Invalid("validation.errorCode.empty")
    }
  }


  "nonEmptyValidText" should {
    val regex = """^[A-Za-z]{1,10}$""".r

    "return valid when string matches regex" in {
      val constraint = FormValidation.nonEmptyValidText("fieldName", regex)
      constraint("abcdef") shouldBe Valid
    }

    "return invalid when string does not match regex" in {
      val constraint = FormValidation.nonEmptyValidText("fieldName", regex)
      constraint("a123") shouldBe Invalid("validation.fieldName.invalid")
    }

    "return invalid when string is empty" in {
      val constraint = FormValidation.nonEmptyValidText("fieldName", regex)
      constraint("") shouldBe Invalid("validation.fieldName.empty")
    }
  }


  "patternCheckingConstraint" must {

    val constraintMandatory = FormValidation.patternCheckingConstraint("""[a-z]{3}""".r, "errorCode", mandatory = true)
    val constraintNonMandatory = FormValidation.patternCheckingConstraint("""[a-z]{3}""".r, "errorCode", mandatory = false)

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
}