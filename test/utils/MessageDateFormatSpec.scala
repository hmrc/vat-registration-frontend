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
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}

import java.time.LocalDate

class MessageDateFormatSpec extends PlaySpec with GuiceOneAppPerSuite with FeatureSwitching {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "date format" should {
    "return welsh translated months" when {
      "welsh language choice is welsh and FS enabled" in {
        enable(WelshLanguage)
        implicit val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))
        (1 to 12).foreach(month => {
          val date = LocalDate.now().withMonth(month)
          MessageDateFormat.format(date) must include(MessageDateFormat.monthsInCy(month))
        })
        disable(WelshLanguage)
      }
    }

    "return english months" when {
      "welsh language choice is welsh and but FS not enabled" in {
        implicit val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))
        (1 to 12).foreach(month => {
          val date = LocalDate.now().withMonth(month)
          MessageDateFormat.format(date) must not include MessageDateFormat.monthsInCy(month)
        })
      }

      "language choice is english" in {
        implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
        (1 to 12).foreach(month => {
          val date = LocalDate.now().withMonth(month)
          MessageDateFormat.format(date) must not include MessageDateFormat.monthsInCy(month)
        })
      }
    }
  }
}
