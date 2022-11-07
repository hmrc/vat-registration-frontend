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
import support.AppAndStubs
import uk.gov.hmrc.http.UpstreamErrorResponse

class VatRegistrationConnectorISpec extends IntegrationSpecBase
  with AppAndStubs
  with ITRegistrationFixtures {

  def vatregConnector: VatRegistrationConnector = app.injector.instanceOf(classOf[VatRegistrationConnector])

  val testRegistrationsResponse = List(
    VatSchemeHeader(
      registrationId = "1",
      createdDate = testCreatedDate,
      status = VatRegStatus.draft,
      requiresAttachments = false
    ),
    VatSchemeHeader(
      registrationId = "2",
      createdDate = testCreatedDate,
      status = VatRegStatus.draft,
      requiresAttachments = false
    )
  )

  override def additionalConfig: Map[String, String] =
    Map(
      "default-incorporation-info" -> "ew0KICAic3RhdHVzRXZlbnQiOiB7DQogICAgImNybiI6ICI5MDAwMDAwMSIsDQogICAgImluY29ycG9yYXRpb25EYXRlIjogIjIwMTYtMDgtMDUiLA0KICAgICJzdGF0dXMiOiAiYWNjZXB0ZWQiDQogIH0sDQogICJzdWJzY3JpcHRpb24iOiB7DQogICAgImNhbGxiYWNrVXJsIjogIiMiLA0KICAgICJyZWdpbWUiOiAidmF0IiwNCiAgICAic3Vic2NyaWJlciI6ICJzY3JzIiwNCiAgICAidHJhbnNhY3Rpb25JZCI6ICIwMDAtNDM0LTEiDQogIH0NCn0="
    )

  "creating new Vat Registration" should {
    "work without problems" when {
      "a registration is already present in the backend" in {
        given()
          .registrationApi.registrationCreated()

        await(vatregConnector.createNewRegistration) mustBe VatScheme(registrationId = "1", status = VatRegStatus.draft, createdDate = testCreatedDate)
      }
    }

    "throw an upstream 5xx exception" when {
      "remote service fails to handle the request" in {
        given()
          .registrationApi.registrationCreationFailed

        intercept[UpstreamErrorResponse] {
          await(vatregConnector.createNewRegistration)
        }
      }
    }
  }

  "submitRegistration" should {
    "return success response and not submit to the backend for an allowed regId" in {
      given().vatRegistration.submit(s"/vatreg/$testRegId/submit-registration", OK)
      val res = vatregConnector.submitRegistration(testRegId, Map("testHeaderKey" -> "testHeaderValue"), "en")(hc)
      await(res) mustBe Success
    }
    "redirect to the contact page when the registration cannot be processed" in {
      given()
        .vatRegistration.submit(s"/vatreg/$testRegId/submit-registration", UNPROCESSABLE_ENTITY)

      val res = await(vatregConnector.submitRegistration(testRegId, Map("testHeaderKey" -> "testHeaderValue"), "en")(hc))
      
      res mustBe Contact
    }
  }

  "getAllRegistrations" should {
    "return OK with a list of registrations" in {
      given().registrationApi.getAllRegistrations(testRegistrationsResponse)

      val res = await(vatregConnector.getAllRegistrations)

      res mustBe List(
        VatSchemeHeader(testRegId, VatRegStatus.draft, createdDate = testCreatedDate, requiresAttachments = false),
        VatSchemeHeader("2", VatRegStatus.draft, createdDate = testCreatedDate, requiresAttachments = false)
      )
    }
    "return OK with a registration that has an application reference" in {
      val testAppRef = "testAppRef"
      given().registrationApi.getAllRegistrations(List(VatSchemeHeader(
        registrationId = "1",
        createdDate = testCreatedDate,
        status = VatRegStatus.draft,
        applicationReference = Some(testAppRef),
        requiresAttachments = false
      )))

      val res = await(vatregConnector.getAllRegistrations)

      res mustBe List(
        VatSchemeHeader(testRegId, VatRegStatus.draft, Some(testAppRef), testCreatedDate, requiresAttachments = false))
    }
  }

}
