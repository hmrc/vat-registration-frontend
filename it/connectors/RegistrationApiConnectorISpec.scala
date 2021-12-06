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

import common.enums.VatRegStatus
import fixtures.ITRegistrationFixtures
import itutil.IntegrationSpecBase
import models.api.{EligibilitySubmissionData, VatScheme}
import play.api.test.Helpers._
import support.AppAndStubs
import uk.gov.hmrc.http.{InternalServerException, Upstream5xxResponse}

class RegistrationApiConnectorISpec extends IntegrationSpecBase with AppAndStubs with ITRegistrationFixtures {

  val connector: RegistrationApiConnector =  app.injector.instanceOf[RegistrationApiConnector]

  "getSection" should {
    "return the requested model if the backend returns OK" in {
        given()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData), testRegId)

        await(connector.getSection[EligibilitySubmissionData](testRegId)) mustBe Some(testEligibilitySubmissionData)
    }

    "return None if the backend returns NOT_FOUND" in {
      given()
        .registrationApi.getSection[EligibilitySubmissionData](None, testRegId)

      await(connector.getSection[EligibilitySubmissionData](testRegId)) mustBe None
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.getSectionFails[EligibilitySubmissionData](testRegId)

      intercept[InternalServerException](await(connector.getSection[EligibilitySubmissionData](testRegId)))
    }
  }

  "replaceSection" should {
    "return the stored model if the backend returns OK" in {
      given()
        .registrationApi.replaceSection[EligibilitySubmissionData](testEligibilitySubmissionData, testRegId)

      await(connector.replaceSection[EligibilitySubmissionData](testRegId, testEligibilitySubmissionData)) mustBe testEligibilitySubmissionData
    }

    "throw an exception on an unexpected response" in {
      given()
        .registrationApi.replaceSectionFails[EligibilitySubmissionData](testRegId)

      intercept[InternalServerException](await(connector.replaceSection[EligibilitySubmissionData](testRegId, testEligibilitySubmissionData)))
    }
  }
}

