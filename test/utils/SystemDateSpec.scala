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

package utils

import java.time.{LocalDate, LocalDateTime}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

class SystemDateSpec extends PlaySpec with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    System.clearProperty("feature.system-date")
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    System.clearProperty("feature.system-date")
  }

  def testLocalDateTime = LocalDateTime.now

  "getSystemDate" should {
    "return a LocalDate of today" when {
      "the feature is null" in {
        val result = SystemDate.getSystemDate
        result.getHour      mustBe testLocalDateTime.getHour
        result.toLocalDate  mustBe LocalDate.now
      }

      "the feature is ''" in {
        System.setProperty("feature.system-date", "")

        val result = SystemDate.getSystemDate
        result.getHour      mustBe testLocalDateTime.getHour
        result.toLocalDate  mustBe LocalDate.now
      }
    }

    "return a LocalDate that was previously set (with specific time)" in {
      System.setProperty("feature.system-date", "2018-01-01T01:43:23")

      val result = SystemDate.getSystemDate
      result.toLocalDate mustBe LocalDate.parse("2018-01-01")
      result             mustBe LocalDateTime.parse("2018-01-01T01:43:23")
    }
  }
}
