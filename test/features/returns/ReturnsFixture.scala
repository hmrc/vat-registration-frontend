/*
 * Copyright 2019 HM Revenue & Customs
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

package features.returns

import java.time.LocalDate

import features.returns.models.{Frequency, Returns, Start}

import scala.language.postfixOps

trait ReturnsFixture {

  val date = LocalDate.now()

  val reclaimOnReturns = true
  val returnsFrequency = Frequency.monthly
  val startDate        = date
  val returns = Returns(Some(reclaimOnReturns), Some(returnsFrequency), None, Some(Start(Some(startDate))))

}
