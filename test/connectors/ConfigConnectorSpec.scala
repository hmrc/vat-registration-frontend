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

package connectors

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.BusinessSectorView
import models.api.SicCode
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when

class ConfigConnectorSpec extends VatRegSpec with VatRegistrationFixture {

  class Setup {
    val connector = new ConfigConnector(mockConfig)
    val sicCode = SicCode(id = "01490025", description = "Silk worm raising", displayDetails = "Raising of other animals")
  }

  "Calling getSicCodeDetails" must {
    "return a SicCode successfully" in new Setup {
      when(mockConfig.getString(ArgumentMatchers.any()))
        .thenReturn("Silk worm raising", "Raising of other animals")

      connector.getSicCodeDetails("01490025") mustBe sicCode
    }
  }

  "Calling getBusinessSectorDetails" must {
    "return a BusinessSectorView successfully" in new Setup {
      when(mockConfig.getString(ArgumentMatchers.any()))
        .thenReturn("Farming or agriculture that is not listed elsewhere", "6.5")

      connector.getBusinessSectorDetails("01490025") mustBe BusinessSectorView("Farming or agriculture that is not listed elsewhere", 6.5)
    }
  }

}
