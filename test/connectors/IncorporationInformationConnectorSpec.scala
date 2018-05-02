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

import config.WSHttp
import helpers.VatRegSpec
import models.external.{CoHoRegisteredOfficeAddress, Name, Officer, OfficerList}
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HttpResponse, Upstream4xxResponse, Upstream5xxResponse}

class IncorporationInformationConnectorSpec extends VatRegSpec {

  class Setup {
    val connector = new IncorporationInformationConnector {
      override val incorpInfoUrl: String = "tst-url"
      override val incorpInfoUri: String = "tst-url"
      override val vatRegFEUrl: String = "tst-url"
      override val http: WSHttp = mockWSHttp
    }

    val testAddress = CoHoRegisteredOfficeAddress(
      "premises", "addressLine1", None,
      "locality", None, None, None, None
    )
  }

  "Calling getRegisteredOfficeAddress" should {
    "return a CoHoRegisteredOfficeAddress successfully" in new Setup {
      mockHttpGET[CoHoRegisteredOfficeAddress]("tst-url", testAddress)
      connector.getRegisteredOfficeAddress("id") returnsSome testAddress
    }

    "return the correct response when an Internal Server Error occurs" in new Setup {
      mockHttpFailedGET[CoHoRegisteredOfficeAddress]("test-url", internalServiceException)
      connector.getRegisteredOfficeAddress("id").returnsNone
    }
  }


  "Calling getOfficerList" should {
    val officerName = Name(None, None, "TestName")
    val testDateTime = DateTime.parse("2017-3-21")

    def director(retired: Boolean = false) = Officer(officerName, "director", resignedOn = if(retired) Some(testDateTime) else None)
    def secretary(retired: Boolean = false) = Officer(officerName, "secretary", resignedOn = if(retired) Some(testDateTime) else None)

    val officerList = OfficerList(Seq(director(retired = true), director(), secretary(), secretary(retired = true)))
    val filteredList = OfficerList(Seq(director(), secretary()))
    val emptyList = OfficerList(Seq.empty)

    "return an non-empty OfficerList" in new Setup {
      mockHttpGET[OfficerList]("tst-url", officerList)
      connector.getOfficerList("id") returnsSome filteredList
    }

    "return an empty OfficerList" in new Setup {
      mockHttpGET[OfficerList]("tst-url", emptyList)
      connector.getOfficerList("id") returnsSome emptyList
    }

    "return empty OfficerList when remote service responds with 404" in new Setup {
      mockHttpFailedGET[OfficerList]("test-url", notFound)
      connector.getOfficerList("id") returnsSome emptyList
    }

    "fail with exception when an Internal Server Error occurs calling remote service" in new Setup {
      mockHttpFailedGET[OfficerList]("test-url", internalServiceException)
      connector.getOfficerList("id") failedWith internalServiceException
    }
  }

  "registerInterest" should {
    "succeed" when {
      "creating a vat frontend subscription in II" in new Setup {
        mockHttpPOST[JsObject, HttpResponse]("tst-url", HttpResponse(202), mockWSHttp)

        val response: Either[String, HttpResponse] = await(connector.registerInterest("regId", "transID"))

        response.right.get.status mustBe 202
      }
      "a vat frontend subscription already exists in II" in new Setup {
        mockHttpPOST[JsObject, HttpResponse]("tst-url", HttpResponse(200, Some(Json.obj("transaction_status" ->"accepted"))))

        val response: Either[String, HttpResponse] = await(connector.registerInterest("regId", "transID"))

        response.right.get.status mustBe 200
      }
    }
    "fail" when {
      "creating a vat frontend subscription in II" in new Setup {
        mockHttpFailedPOST[JsObject, HttpResponse]("tst-url", Upstream5xxResponse("503", 503, 503), mockWSHttp)

        intercept[Upstream5xxResponse](await(connector.registerInterest("regId", "transID")))
      }
    }
  }

  "cancelSubscription" should {
    "succeed" when {
      "cancelling an existing VRFE subscription" in new Setup {
        mockHttpDELETE[HttpResponse]("tst-url", HttpResponse(200), mockWSHttp)

        val response: HttpResponse = await(connector.cancelSubscription("transID"))

        response.status mustBe 200
      }
    }

    "fail" when {
      "cancelling an non-existant VRFE subscription" in new Setup {
        mockHttpFailedDELETE[HttpResponse]("tst-url", Upstream4xxResponse("404", 404, 404), mockWSHttp)

        intercept[Upstream4xxResponse](await(connector.cancelSubscription("transID")))
      }
    }
  }

}
