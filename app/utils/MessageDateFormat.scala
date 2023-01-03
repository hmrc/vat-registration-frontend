/*
 * Copyright 2023 HM Revenue & Customs
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

import featureswitch.core.config.{FeatureSwitching, WelshLanguage}
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MessageDateFormat extends FeatureSwitching {

  private[utils] val monthsInCy = Map(
    1 -> "Ionawr",
    2 -> "Chwefror",
    3 -> "Mawrth",
    4 -> "Ebrill",
    5 -> "Mai",
    6 -> "Mehefin",
    7 -> "Gorffennaf",
    8 -> "Awst",
    9 -> "Medi",
    10 -> "Hydref",
    11 -> "Tachwedd",
    12 -> "Rhagfyr"
  )

  def format(date: LocalDate)(implicit messages: Messages): String = {
    val formatter = messages.lang.code match {
      case "cy" if isEnabled(WelshLanguage) => DateTimeFormatter.ofPattern(s"d '${monthsInCy(date.getMonthValue)}' yyyy")
      case _ => DateTimeFormatter.ofPattern("d MMMM yyyy")
    }

    date.format(formatter)
  }
}
