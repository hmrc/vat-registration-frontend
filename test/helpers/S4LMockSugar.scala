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

package helpers

import cats.data.OptionT
import models.S4LKey
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import services.S4LService

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

trait S4LMockSugar {
  self: VatRegSpec =>

  import cats.instances.future._
  import cats.syntax.applicative._

  def save4laterReturnsNothing[T: S4LKey]()(implicit s4LService: S4LService, ec: ExecutionContext): Unit =
    when(s4LService.fetchAndGet[T]()(Matchers.eq(S4LKey[T]), any(), any())).thenReturn(None.pure)

  def save4laterReturns[T: S4LKey](t: T)(implicit s4lService: S4LService, ec: ExecutionContext): Unit =
    when(s4lService.fetchAndGet[T]()(Matchers.eq(S4LKey[T]), any(), any())).thenReturn(OptionT.pure(t).value)

}
