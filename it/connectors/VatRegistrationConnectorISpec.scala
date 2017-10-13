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

import models.api.VatScheme
import play.api.mvc.Call
import support.AppAndStubs
import uk.gov.hmrc.play.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.test.UnitSpec

class VatRegistrationConnectorISpec extends UnitSpec with AppAndStubs {

  def vatregConnector: RegistrationConnector = app.injector.instanceOf(classOf[RegistrationConnector])

  "creating new Vat Registration" should {

    "work wihtout problems" when {
      "a registration is already present in the backend" in {
        given()
          .vatRegistrationFootprint.exists()

        await(vatregConnector.createNewRegistration()) shouldBe VatScheme(id="1")
      }
    }

    "throw an upstream 5xx exception" when {
      "remote service fails to handle the request" in {
        given()
          .vatRegistrationFootprint.fails

        intercept[Upstream5xxResponse] {
          await(vatregConnector.createNewRegistration())
        }
      }
    }
  }
}

