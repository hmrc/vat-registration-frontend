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
import models.api.{VatChoice, VatScheme}
import models.view.StartDate
import org.joda.time.format.DateTimeFormat
import uk.gov.hmrc.play.test.UnitSpec

class StartDateSpec extends UnitSpec with VatRegistrationFixture {

  val startDateTime = DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("01/02/2017")
  val startDate = StartDate(StartDate.SPECIFIC_DATE, Some(1), Some(2), Some(2017))
  val newStartDate = StartDate(StartDate.SPECIFIC_DATE, Some(30), Some(12), Some(2001))

  "empty" should {
    "create an empty StartDate model" in {
      StartDate.empty shouldBe StartDate("", None, None, None)
    }
  }

  "toDateTime" should {
    "convert a populated StartDate model to a DateTime" in {
      startDate.toDateTime shouldBe startDateTime
    }
  }

  "toApi" should {
    "update a VatChoice a new StartDate" in {
      val vatChoice = VatChoice(newStartDate.toDateTime, VatChoice.NECESSITY_OBLIGATORY)
      startDate.toApi(vatChoice) shouldBe VatChoice(startDate.toDateTime, VatChoice.NECESSITY_OBLIGATORY)
    }
  }

  "apply" should {
    "extract a StartDate from a VatScheme" in {
      val vatChoice = VatChoice(startDate.toDateTime, VatChoice.NECESSITY_VOLUNTARY)
      val vatScheme = VatScheme(id = validRegId, vatChoice = Some(vatChoice))
      StartDate.apply(vatScheme) shouldBe startDate
    }
  }

  "fromDateTime" should {
    "convert a DateTime object to a StartDate model" in {
      val startDate = StartDate.fromDateTime(startDateTime)
      startDate shouldBe startDate
    }

    // TODO: remove when we play the VatChoice refactoring story
    "convert a DateTime object to a StartDate model when it's a default value" in {
      val defaultDateTime = DateTimeFormat.forPattern("dd/MM/yyyy").parseDateTime("31/12/1969")
      val startDate = StartDate.fromDateTime(defaultDateTime)
      startDate shouldBe StartDate.empty
    }
  }

}
