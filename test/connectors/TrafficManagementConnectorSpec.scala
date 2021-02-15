/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.api.trafficmanagement.{Draft, RegistrationInformation, VatReg}
import testHelpers.VatRegSpec

import java.time.LocalDate

class TrafficManagementConnectorSpec extends VatRegSpec {

  lazy val frontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  object TestTrafficManagementConnector extends TrafficManagementConnector(mockHttpClient, frontendAppConfig)

  "getRegistrationInformation" should {
    "return a valid RegistrationInformation if a user went through TM" in {
      val res = RegistrationInformation("testIntId", "testRegId", Draft, Some(LocalDate.now()), VatReg)
      mockHttpGET[Option[RegistrationInformation]](frontendAppConfig.getRegistrationInformationUrl, Some(res))

      TestTrafficManagementConnector.getRegistrationInformation returns Some(res)
    }

    "return a None if a user did not go through TM" in {
      mockHttpGET[Option[RegistrationInformation]](frontendAppConfig.getRegistrationInformationUrl, None)

      TestTrafficManagementConnector.getRegistrationInformation returns None
    }
  }
}
