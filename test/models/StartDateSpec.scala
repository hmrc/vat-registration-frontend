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

package models

import fixtures.VatRegistrationFixture
import models.view.StartDate
import uk.gov.hmrc.play.test.UnitSpec

class StartDateSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty StartDate model" in {
      StartDate.empty shouldBe StartDate("", None, None, None)
    }
  }

  "toDate" should {
    "convert a populated StartDate model to a DateTime" in {
      val startDate: StartDate = StartDate(StartDate.FUTURE_DATE, Some("01"), Some("02"), Some("17"))
      startDate.toDate shouldBe validDateTime
    }
  }

}
