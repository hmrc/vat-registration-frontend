/*
 * Copyright 2020 HM Revenue & Customs
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

package testHelpers

import cats.data.OptionT
import models.S4LKey
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import services.S4LService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext

trait S4LMockSugar {
  self: VatRegSpec =>

  implicit val dummyCacheMap = CacheMap("", Map.empty)

  def save4laterReturnsNothing[T: S4LKey]()(implicit s4l: S4LService, ec: ExecutionContext): Unit =
    when(s4l.fetchAndGet[T](ArgumentMatchers.eq(S4LKey[T]), any(), any(), any())).thenReturn(None.pure)

  def save4laterReturns[T: S4LKey](t: T)(implicit s4l: S4LService, ec: ExecutionContext): Unit =
    when(s4l.fetchAndGet[T](ArgumentMatchers.eq(S4LKey[T]), any(), any(), any())).thenReturn(OptionT.pure(t).value)

}
