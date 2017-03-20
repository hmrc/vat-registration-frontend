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

package common

import java.time.{LocalDate, LocalDateTime}
import java.util.Date

import org.joda.time.DateTime


trait Now[T] {
  def apply(): T
}

object Now {

  // $COVERAGE-OFF$

  def apply[T]()(implicit now: Now[T]) = now

  def apply[T](value: => T): Now[T] = new Now[T] {
    override def apply(): T = value
  }

  implicit object DateNow extends Now[Date] {
    override def apply(): Date = new Date()
  }

  implicit object JodaDateTimeNow extends Now[DateTime] {
    override def apply(): DateTime = DateTime.now()
  }

  implicit object LocalDateNow extends Now[LocalDate] {
    override def apply(): LocalDate = LocalDate.now()
  }

  implicit object LocalDateTimeNow extends Now[LocalDateTime] {
    override def apply(): LocalDateTime = LocalDateTime.now()
  }

  // $COVERAGE-ON$

}
