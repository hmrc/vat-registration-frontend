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

import com.github.tomakehurst.wiremock.client.WireMock.{getRequestedFor, urlEqualTo, verify}
import itFixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.ApiKey
import models.api.EligibilitySubmissionData
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.InternalServerException

class RegistrationApiConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: RegistrationApiConnector =  app.injector.instanceOf[RegistrationApiConnector]

  "getSection" should {
    "return the requested model if the backend returns OK" in {
        given()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData), testRegId)

        await(connector.getSection[EligibilitySubmissionData](testRegId)) mustBe Some(testEligibilitySubmissionData)
        verify(getRequestedFor(urlEqualTo(s"/vatreg/registrations/$testRegId/sections/${ApiKey[EligibilitySubmissionData]}")))
    }

    "return the indexed requested model when index is passed" in {
      val index = 1
      given()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData), testRegId, idx = Some(index))

      await(connector.getSection[EligibilitySubmissionData](testRegId, idx = Some(index))) mustBe Some(testEligibilitySubmissionData)
      verify(getRequestedFor(urlEqualTo(s"/vatreg/registrations/$testRegId/sections/${ApiKey[EligibilitySubmissionData]}/$index")))
    }

    "return None if the backend returns NOT_FOUND" in {
      given()
        .registrationApi.getSection[EligibilitySubmissionData](None, testRegId)

      await(connector.getSection[EligibilitySubmissionData](testRegId)) mustBe None
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.getSectionFails[EligibilitySubmissionData](testRegId)

      val exception = intercept[InternalServerException](await(connector.getSection[EligibilitySubmissionData](testRegId)))
      exception.getMessage must include("Unexpected response:")
    }
  }

  "getListSection" should {
    "return the requested model if the backend returns OK" in {
      given()
        .registrationApi.getListSection[EligibilitySubmissionData](Some(List(testEligibilitySubmissionData, testEligibilitySubmissionData)), testRegId)

      await(connector.getListSection[EligibilitySubmissionData](testRegId)) mustBe List(testEligibilitySubmissionData, testEligibilitySubmissionData)
      verify(getRequestedFor(urlEqualTo(s"/vatreg/registrations/$testRegId/sections/${ApiKey[EligibilitySubmissionData]}")))
    }

    "return empty list if the backend returns NOT_FOUND" in {
      given()
        .registrationApi.getListSection[EligibilitySubmissionData](None, testRegId)

      await(connector.getListSection[EligibilitySubmissionData](testRegId)) mustBe Nil
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.getSectionFails[EligibilitySubmissionData](testRegId)

      val exception = intercept[InternalServerException](await(connector.getListSection[EligibilitySubmissionData](testRegId)))
      exception.getMessage must include("Unexpected response:")
    }
  }

  "replaceSection" should {
    "return the stored model if the backend returns OK" in {
      given()
        .registrationApi.replaceSection[EligibilitySubmissionData](testEligibilitySubmissionData, testRegId)

      await(connector.replaceSection[EligibilitySubmissionData](testRegId, testEligibilitySubmissionData)) mustBe testEligibilitySubmissionData
    }

    "return the indexed stored model when index is passed" in {
      val index = 1
      given()
        .registrationApi.replaceSection[EligibilitySubmissionData](testEligibilitySubmissionData, testRegId, Some(index))

      await(connector.replaceSection[EligibilitySubmissionData](testRegId, testEligibilitySubmissionData, idx = Some(index))) mustBe testEligibilitySubmissionData
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.replaceSectionFails[EligibilitySubmissionData](testRegId)

      val exception = intercept[InternalServerException](await(connector.replaceSection[EligibilitySubmissionData](testRegId, testEligibilitySubmissionData)))
      exception.getMessage must include("Unexpected response:")
    }
  }

  "replaceListSection" should {
    "return the stored model if the backend returns OK" in {
      given()
        .registrationApi.replaceListSection[EligibilitySubmissionData](List(testEligibilitySubmissionData), testRegId)

      await(connector.replaceListSection[EligibilitySubmissionData](testRegId,List(testEligibilitySubmissionData))) mustBe List(testEligibilitySubmissionData)
    }

    "return the indexed stored model when index is passed" in {
      val index = 1
      given()
        .registrationApi.replaceListSection[EligibilitySubmissionData](List(testEligibilitySubmissionData), testRegId)

      await(connector.replaceListSection[EligibilitySubmissionData](testRegId, List(testEligibilitySubmissionData))) mustBe List(testEligibilitySubmissionData)
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.replaceListSection[EligibilitySubmissionData](List(), testRegId)

      val exception = intercept[InternalServerException](await(connector.replaceListSection[EligibilitySubmissionData](testRegId, List(testEligibilitySubmissionData))))
      exception.getMessage must include("Unexpected response:")
    }
  }
}

