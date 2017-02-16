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
import models.api.VatChoice
import models.view.StartDate
import uk.gov.hmrc.play.test.UnitSpec

class StartDateSpec extends UnitSpec with VatRegistrationFixture {

  "empty" should {
    "create an empty StartDate model" in {
      StartDate.empty shouldBe StartDate("", None, None, None)
    }
  }

  "toDateTime" should {
    "convert a populated StartDate model to a DateTime" in {
      validStartDate.toDateTime shouldBe validDateTime
    }
  }

  "toApi" should {
    "upserts (merge) a current VatChoice API model with the details of an instance of StartDate view model" in {
      val vatChoice = VatChoice(
       StartDate(StartDate.SPECIFIC_DATE, Some(30), Some(12), Some(2001)).toDateTime,
        VatChoice.NECESSITY_OBLIGATORY
      )

      validStartDate.toApi(vatChoice) shouldBe VatChoice(
        validStartDate.toDateTime,
        VatChoice.NECESSITY_OBLIGATORY
      )
    }
  }

  "apply" should {
    "convert a populated VatScheme's VatChoice API model to an instance of StartDate view model" in {
      StartDate.apply(validVatScheme) shouldBe validStartDate
    }
  }

  "fromDateTime" should {
    "convert a DateTime object to a StartDate model" in {
      val startDate = StartDate.fromDateTime(validDateTime)
      startDate shouldBe validStartDate
    }

    // TODO: remove when we play the VatChoice refactoring story
    "convert a DateTime object to a StartDate model when it's a default value" in {
      val startDate = StartDate.fromDateTime(validDefaultDateTime)
      startDate shouldBe StartDate.empty
    }
  }

}
