/*
 * Copyright 2022 HM Revenue & Customs
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

import models.api.SicCode
import models.{FrsBusinessType, FrsGroup}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.Environment
import testHelpers.{VatMocks, VatRegSpec}

import java.util.MissingResourceException

class ConfigConnectorSpec extends VatRegSpec with VatMocks {
  val mockEnvironment: Environment = mock[Environment]

  class Setup {
    val connector: ConfigConnector = new ConfigConnector(
      mockServicesConfig,
      mockEnvironment
    ) {
      override lazy val businessTypes: Seq[FrsGroup] = Seq(
        FrsGroup(
          label = "Accommodation and food service activities",
          labelCy = "Accommodation and food service activities",
          categories = List(
            FrsBusinessType(id = "020", label = "Hotel or accommodation", labelCy = "Hotel or accommodation", percentage = 10.5),
            FrsBusinessType(id = "008", label = "Catering services including restaurants and takeaways", labelCy = "Catering services including restaurants and takeaways", percentage = 12.5),
            FrsBusinessType(id = "038", label = "Pubs", labelCy = "Pubs", percentage = 6.5)
          )
        )
      )
    }
    val sicCode = SicCode(code = "01490001", description = "Silk worm raising", displayDetails = "Raising of other animals")
  }

  "Calling getSicCodeDetails" must {
    "return a SicCode successfully" in new Setup {
      when(mockServicesConfig.getString(ArgumentMatchers.any()))
        .thenReturn("Silk worm raising", "Raising of other animals")

      connector.getSicCodeDetails("01490") mustBe sicCode
    }
  }

  "Calling getSicCodeFRSCategory" must {
    "return a FRS Category ID" in new Setup {
      when(mockServicesConfig.getString(ArgumentMatchers.any()))
        .thenReturn("055")

      connector.getSicCodeFRSCategory("01490") mustBe "055"
    }
  }

  "Calling getBusinessTypeDetails" must {
    "return a BusinessSectorView successfully" in new Setup {
      val id = "038"
      val businessType = "Pubs"
      val percent = 6.5

      connector.getBusinessType(id) mustBe FrsBusinessType(id, businessType, businessType, percent)
    }

    "does Not return a BusinessSectorView, instead throws an exception" in new Setup {
      val id = "000"

      a[MissingResourceException] mustBe thrownBy(connector.getBusinessType(id))
    }
  }

}
