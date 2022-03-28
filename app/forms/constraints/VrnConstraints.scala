/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.constraints

import play.api.data.validation.{Constraint, Invalid, Valid}

case object VrnConstraints {

  private def calcWeightedSum(value: String): Int = {
    // not efficient but saves writing out a hardcoded calculation or a recursive function
    val constants = (2 to 8).reverse
    value.map(_.asDigit).zip(constants)
      .map { case (digit, constant) => digit * constant }.sum
  }

  def isValidChecksum(errorKey: String): Constraint[String] = {
    Constraint { vatNumber =>
      val leading = vatNumber.substring(0, vatNumber.length - 2)
      val checksum = vatNumber.substring(vatNumber.length - 2).toInt

      val weightedSumPlusChecksum = calcWeightedSum(leading) + checksum

      if ((weightedSumPlusChecksum % 97) == 0 || ((weightedSumPlusChecksum + 55) % 97) == 0) {
        Valid
      } else {
        Invalid(errorKey)
      }
    }
  }

}
