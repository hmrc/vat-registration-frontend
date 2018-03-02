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

import java.util.MissingResourceException

import mocks.VatMocks
import models.api.SicCode
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.play.config.inject.ServicesConfig

class ConfigConnectorSpec extends PlaySpec with MockitoSugar with VatMocks{

  class Setup {
    val connector = new ConfigConnector {
      override val config: ServicesConfig = mockConfig
    }
    val sicCode = SicCode(id = "01490025", description = "Silk worm raising", displayDetails = "Raising of other animals")
  }

  "Calling getSicCodeDetails" must {
    "return a SicCode successfully" in new Setup {
      when(mockConfig.getString(ArgumentMatchers.any()))
        .thenReturn("Silk worm raising", "Raising of other animals")

      connector.getSicCodeDetails("01490025") mustBe sicCode
    }
  }

  "Calling getSicCodeFRSCategory" must {
    "return a FRS Category ID" in new Setup {
      when(mockConfig.getString(ArgumentMatchers.any()))
          .thenReturn("055")

      connector.getSicCodeFRSCategory("01490025") mustBe "055"
    }
  }

  "Calling getBusinessTypeDetails" must {
    "return a BusinessSectorView successfully" in new Setup {
      val id = "055"
      val businessType = "Farming or agriculture that is not listed elsewhere"
      val percent = 6.5
      val jsonBusinessTypes =
        s"""
          |[
          |  {
          |    "groupLabel": "Test 1",
          |    "categories": [
          |      {"id": "020", "businessType": "Hotel or accommodation", "currentFRSPercent": 10.5},
          |      {"id": "$id", "businessType": "$businessType", "currentFRSPercent": $percent},
          |      {"id": "038", "businessType": "Pubs", "currentFRSPercent": "5"}
          |    ]
          |  },
          |  {
          |    "groupLabel": "Test 2",
          |    "categories": [
          |      {"id": "039", "businessType": "Cafes", "currentFRSPercent": "5"}
          |    ]
          |  }
          |]
        """.stripMargin

      when(mockConfig.getString(ArgumentMatchers.any()))
        .thenReturn(jsonBusinessTypes)

      connector.getBusinessTypeDetails(id) mustBe (businessType, percent)
    }
    "does Not return a BusinessSectorView, instead throws an exception" in new Setup {
      val id = "050"
      val jsonContainingNoID =
        s"""
           |[
           |  {
           |    "groupLabel": "Test 1",
           |    "categories": [
           |      {"id": "100", "businessType": "Hotel or accommodation", "currentFRSPercent": 10.5}
           |     ]
           |  }
           |]
         """.stripMargin

      when(mockConfig.getString(ArgumentMatchers.any()))
        .thenReturn(jsonContainingNoID)

      a[MissingResourceException] mustBe thrownBy(connector.getBusinessTypeDetails(id))
    }
  }

}
