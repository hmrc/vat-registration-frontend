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

package connectors

import fixtures.VatRegistrationFixture
import helpers.VatRegSpec
import models.api.Name
import models.external.{CoHoRegisteredOfficeAddress, Officer, OfficerList}
import org.joda.time.DateTime
import uk.gov.hmrc.play.http.ws.WSHttp

class IncorporationInformationConnectorSpec extends VatRegSpec {

  class Setup {
    val connector = new IncorporationInformationConnect {
      override val incorpInfoUrl: String = "tst-url"
      override val incorpInfoUri: String = "tst-url"
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

}

