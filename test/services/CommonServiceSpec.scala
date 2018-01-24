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

package services

import connectors.KeystoreConnector
import helpers.VatRegSpec

class CommonServiceSpec extends VatRegSpec {

  trait Setup {
    val service = new CommonService {
      override val keystoreConnector: KeystoreConnector = mockKeystoreConnect
    }
  }

  "Calling fetchDateOfIncorporation" should {
    "throw an IllegalStateException when no Incorporation Date is found in keystore" in new Setup {
      mockKeystoreFetchAndGet[String]("incorporationStatus", None)
      service.fetchDateOfIncorporation failedWith classOf[IllegalStateException]
    }
  }
}
