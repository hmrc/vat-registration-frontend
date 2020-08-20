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

package connectors

import java.util.MissingResourceException

import mocks.VatMocks
import models.api.SicCode
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Environment
import play.api.libs.json.{JsObject, Json}
import testHelpers.VatRegSpec

class ConfigConnectorSpec extends VatRegSpec with VatMocks {
  val mockEnvironment: Environment = mock[Environment]

  class Setup {
    val connector: ConfigConnector = new ConfigConnector(
      mockServicesConfig,
      mockEnvironment
    ) {
      override lazy val businessTypes: Seq[JsObject] = Seq(
        Json.parse(
          """
            |{
            |  "groupLabel": "Accommodation and food service activities",
            |  "categories": [
            |    {"id": "020", "businessType": "Hotel or accommodation", "currentFRSPercent": 10.5},
            |    {"id": "008", "businessType": "Catering services including restaurants and takeaways", "currentFRSPercent": 12.5},
            |    {"id": "038", "businessType": "Pubs", "currentFRSPercent": 6.5}
            |  ]
            |}
          """.stripMargin).as[JsObject]
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

      connector.getBusinessTypeDetails(id) mustBe(businessType, percent)
    }

    "does Not return a BusinessSectorView, instead throws an exception" in new Setup {
      val id = "000"

      a[MissingResourceException] mustBe thrownBy(connector.getBusinessTypeDetails(id))
    }
  }

}
