/*
 * Copyright 2026 HM Revenue & Customs
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

package models.bars

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BarsErrorsSpec extends AnyWordSpec with Matchers {

  "BarsErrors.values" should {

    "contain all error variants exactly once" in {
      BarsErrors.values.toSet shouldBe Set(
        BarsErrors.BankAccountUnverified,
        BarsErrors.AccountDetailInvalidFormat,
        BarsErrors.SortCodeNotFound,
        BarsErrors.SortCodeNotSupported,
        BarsErrors.AccountNotFound,
        BarsErrors.NameMismatch,
        BarsErrors.SortCodeOnDenyList,
        BarsErrors.DetailsVerificationFailed
      )

      BarsErrors.values.distinct.size shouldBe BarsErrors.values.size
    }
  }
}