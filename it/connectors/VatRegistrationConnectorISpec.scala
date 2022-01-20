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
import models.api.{VatScheme, VatSchemeHeader}
import play.api.test.Helpers._
import support.{APIStub, AppAndStubs, CanGet, RegistrationsApiStubs}
import uk.gov.hmrc.http.Upstream5xxResponse
import play.api.libs.json.Json

class VatRegistrationConnectorISpec extends IntegrationSpecBase
  with AppAndStubs
  with ITRegistrationFixtures
  with RegistrationsApiStubs {

  def vatregConnector: VatRegistrationConnector = app.injector.instanceOf(classOf[VatRegistrationConnector])

  val registrationsStub = new APIStub("/vatreg/registrations") with CanGet
  val testRegistrationsResponse = Json.arr(Json.toJson(fullVatScheme), Json.toJson(fullVatScheme.copy(id = "2")))

  override def additionalConfig: Map[String, String] =
    Map(
      "default-incorporation-info" -> "ew0KICAic3RhdHVzRXZlbnQiOiB7DQogICAgImNybiI6ICI5MDAwMDAwMSIsDQogICAgImluY29ycG9yYXRpb25EYXRlIjogIjIwMTYtMDgtMDUiLA0KICAgICJzdGF0dXMiOiAiYWNjZXB0ZWQiDQogIH0sDQogICJzdWJzY3JpcHRpb24iOiB7DQogICAgImNhbGxiYWNrVXJsIjogIiMiLA0KICAgICJyZWdpbWUiOiAidmF0IiwNCiAgICAic3Vic2NyaWJlciI6ICJzY3JzIiwNCiAgICAidHJhbnNhY3Rpb25JZCI6ICIwMDAtNDM0LTEiDQogIH0NCn0="
    )

  "creating new Vat Registration" should {
    "work without problems" when {
      "a registration is already present in the backend" in {
        given()
          .vatRegistrationFootprint.exists()

        await(vatregConnector.createNewRegistration) mustBe VatScheme(id = "1", status = VatRegStatus.draft)
      }
      "a registration with a createdDate is already present in the backend" in {
        given()
          .vatRegistrationFootprint.exists(withDate = true)

        await(vatregConnector.createNewRegistration) mustBe VatScheme(id = "1", status = VatRegStatus.draft, createdDate = Some(testCreatedDate))
      }
    }

    "throw an upstream 5xx exception" when {
      "remote service fails to handle the request" in {
        given()
          .vatRegistrationFootprint.fails

        intercept[Upstream5xxResponse] {
          await(vatregConnector.createNewRegistration)
        }
      }
    }
  }

  "submitRegistration" should {

    "return success response and not submit to the backend for an allowed regId" in {
      given().vatRegistration.submit("/vatreg/99/submit-registration", OK)
      val res = vatregConnector.submitRegistration("99", Map("testHeaderKey" -> "testHeaderValue"))(hc)
      await(res) mustBe Success
    }
  }

  "getAckRef" should {
    "return an acknowledgement reference" in {
      val testAckRef = "testAckRef"
      given().vatRegistration.acknowledgementReference("99", testAckRef)
      val res = vatregConnector.getAckRef("99")(hc)
      await(res) mustBe testAckRef
    }
  }

  "submitHonestyDeclaration" should {
    "return an OK status code" in {
      val testHonestyDeclaration = "testHonestyDeclaration"
      given().vatRegistration.honestyDeclaration("99", testHonestyDeclaration)
      val res = vatregConnector.submitHonestyDeclaration("99", true)(hc)
      await(res).status mustBe OK
    }
  }

  "getAllRegistrations" should {
    "return OK with a list of registrations" in {
      registrationsStub.GET.respondsWith(OK, Some(testRegistrationsResponse))

      val res = await(vatregConnector.getAllRegistrations)

      res mustBe List(VatSchemeHeader(testRegId, VatRegStatus.draft, requiresAttachments = false), VatSchemeHeader("2", VatRegStatus.draft, requiresAttachments = false))
    }
    "return OK with a registration that has an application reference and start date" in {
      val testAppRef = "testAppRef"

      registrationsStub.GET.respondsWith(OK, Some(Json.arr(Json.toJson(fullVatScheme.copy(
        applicationReference = Some(testAppRef),
        createdDate = Some(testCreatedDate)
      )))))

      val res = await(vatregConnector.getAllRegistrations)

      res mustBe List(VatSchemeHeader(testRegId, VatRegStatus.draft, Some(testAppRef), Some(testCreatedDate), requiresAttachments = false))
    }
  }

}
