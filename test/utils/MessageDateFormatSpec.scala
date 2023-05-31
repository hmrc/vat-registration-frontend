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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}

import java.time.LocalDate

class MessageDateFormatSpec extends PlaySpec with GuiceOneAppPerSuite {
  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  "date format" should {
    "return welsh translated months" when {
      "language choice is welsh" in {
        implicit val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))
        (1 to 12).foreach(month => {
          val date = LocalDate.now().withMonth(month)
          MessageDateFormat.format(date) must include(MessageDateFormat.monthsInCy(month))
        })
      }
    }

    "return english months" when {
      "language choice is english" in {
        implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))
        (1 to 12).foreach(month => {
          val date = LocalDate.now().withMonth(month)
          MessageDateFormat.format(date) must not include MessageDateFormat.monthsInCy(month)
        })
      }
    }

    "return numeric months" when {
      "using formatNoText function" in {
          val date = LocalDate.parse("2019-01-01")
          MessageDateFormat.formatNoText(date) mustBe "01 01 2019"
      }
    }
  }
}
