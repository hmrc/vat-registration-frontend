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

package utils

import forms.vatDetails.{EstimateVatTurnoverForm, EstimateZeroRatedSalesForm, TradingNameForm}
import helpers.VatRegSpec
import models.view.TradingName
import org.apache.commons.lang3.StringUtils
import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.matching.Regex

class VatValidatorsSpec extends VatRegSpec {

    val testTurnoverEstimateForm = EstimateVatTurnoverForm.form
    val zeroRatedSalesEstimateForm = EstimateZeroRatedSalesForm.form


  //Estimate Vat Turnover Page Form
  "return an error when user enters a empty turnover estimate" in {
    val data : Map[String, String] =
      Map(
        "turnoverEstimate" -> ""
      )

    val boundForm = testTurnoverEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", "pages.estimate.vat.turnover.validation.empty"))
  }

  "return an error when user enters a turnover estimate greater than 1,000,000,000,000,000" in {
    val data : Map[String, String] =
      Map(
        "turnoverEstimate" -> "1000000000000001"
      )

    val boundForm = testTurnoverEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", VatValidators.TURNOVER_ESTIMATE_HIGH_MSG_KEY))
  }

  "return an error when user enters a turnover estimate less than 0" in {
    val data : Map[String, String] =
      Map(
        "turnoverEstimate" -> "-1"
      )

    val boundForm = testTurnoverEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", VatValidators.TURNOVER_ESTIMATE_LOW_MSG_KEY))
  }

  "return no errors when user enters a valid turnover estimate" in {
    val data : Map[String, String] =
      Map(
        "turnoverEstimate" -> "50000"
      )

    val boundForm = testTurnoverEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List()
  }

  //Estimate Zero Rated Vat Turnover Page Form
  "return an error when user enters a empty zero-rated sales estimate" in {
    val data : Map[String, String] =
      Map(
        "zeroRatedSalesEstimate" -> ""
      )

    val boundForm = zeroRatedSalesEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", VatValidators.ZERO_RATED_SALES_ESTIMATE_EMPTY_MSG_KEY))
  }

  "return an error when user enters a zero rated sales greater than 1,000,000,000,000,000" in {
    val data : Map[String, String] =
      Map(
        "zeroRatedSalesEstimate" -> "1000000000000001"
      )

    val boundForm = zeroRatedSalesEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", VatValidators.ZERO_RATED_SALES_ESTIMATE_HIGH_MSG_KEY))

  }

  "return an error when user enters a zero rated sales estimate less than 0" in {
    val data : Map[String, String] =
      Map(
        "zeroRatedSalesEstimate" -> "-1"
      )
    val boundForm = zeroRatedSalesEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List(("", VatValidators.ZERO_RATED_SALES_ESTIMATE_LOW_MSG_KEY))
  }

  "return no errors when user enters a valid zero rated sales estimate" in {
    val data : Map[String, String] =
      Map(
        "zeroRatedSalesEstimate" -> "50000"
      )

    val boundForm = zeroRatedSalesEstimateForm.bind(data)
    boundForm.errors.map(err => (err.key, err.message)) mustBe List()
  }

  "nonEmptyValidText" should {
    val regex = """^[A-Za-z]{1,10}$""".r

    "return valid when string matches regex" in {
      val constraint = VatValidators.nonEmptyValidText("fieldName", regex)
      constraint("abcdef") mustBe Valid
    }

    "return invalid when string does not match regex" in {
      val constraint = VatValidators.nonEmptyValidText("fieldName", regex)
      constraint("a123") mustBe Invalid(s"validation.fieldName.invalid")
    }

    "return invalid when string is empty" in {
      val constraint = VatValidators.nonEmptyValidText("fieldName", regex)
      constraint("") mustBe Invalid(s"validation.fieldName.empty")
    }
  }
}
