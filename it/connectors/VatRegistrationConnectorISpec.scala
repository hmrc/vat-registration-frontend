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

import java.time.LocalDate
import java.util.Base64

import common.enums.VatRegStatus
import models.api.VatScheme
import models.external.{IncorpStatusEvent, IncorpSubscription, IncorporationInfo}
import play.api.libs.json.{JsString, Json}
import support.AppAndStubs
import uk.gov.hmrc.http.Upstream5xxResponse
import uk.gov.hmrc.play.test.UnitSpec

class VatRegistrationConnectorISpec extends UnitSpec with AppAndStubs {

  def vatregConnector: RegistrationConnector = app.injector.instanceOf(classOf[VatRegistrationConnector])

  val nonWhitelistedRegId   = "normalUser"
  val transactionID         = "000-434-1"

  val currentProfileWhitelisted = models.CurrentProfile(
    "fooBar","99","dummy",VatRegStatus.draft,None,None
  )

  def incorpInfo(backUrl: String = "http://localhost:9896/callbackUrl", txId: String = transactionID) = IncorporationInfo(
    IncorpSubscription(
      transactionId = txId,
      regime        = "vat",
      subscriber    = "scrs",
      callbackUrl   = backUrl),
    IncorpStatusEvent(
      status            = "accepted",
      crn               = Some("90000001"),
      incorporationDate = Some(LocalDate.parse("2016-08-05")),
      description       = None))

  val incorpInfoRaw  =
    s"""
      |{
      |  "statusEvent": {
      |    "crn": "90000001",
      |    "incorporationDate": "2016-08-05",
      |    "status": "accepted"
      |  },
      |  "subscription": {
      |    "callbackUrl": "#",
      |    "regime": "vat",
      |    "subscriber": "scrs",
      |    "transactionId": "000-434-1"
      |  }
      |}
    """.stripMargin


  override  def additionalConfig: Map[String, String] =
    Map(
      "regIdPostIncorpWhitelist" -> "OTgsOTk=",
      "regIdPreIncorpWhitelist" -> "MTAyLDEwMw==",
      "default-incorporation-info" -> "ew0KICAic3RhdHVzRXZlbnQiOiB7DQogICAgImNybiI6ICI5MDAwMDAwMSIsDQogICAgImluY29ycG9yYXRpb25EYXRlIjogIjIwMTYtMDgtMDUiLA0KICAgICJzdGF0dXMiOiAiYWNjZXB0ZWQiDQogIH0sDQogICJzdWJzY3JpcHRpb24iOiB7DQogICAgImNhbGxiYWNrVXJsIjogIiMiLA0KICAgICJyZWdpbWUiOiAidmF0IiwNCiAgICAic3Vic2NyaWJlciI6ICJzY3JzIiwNCiAgICAidHJhbnNhY3Rpb25JZCI6ICIwMDAtNDM0LTEiDQogIH0NCn0="
    )

  "creating new Vat Registration" should {

    "work without problems" when {
      "a registration is already present in the backend" in {
        given()
          .vatRegistrationFootprint.exists()

        await(vatregConnector.createNewRegistration) shouldBe VatScheme(id="1", status = VatRegStatus.draft)
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

  "getIncorporationInfo" should {

    "return the default IncorpInfo for a post incorp whitelisted regId" in {
      val res = vatregConnector.getIncorporationInfo("99", transactionID)(hc)
      await(res) shouldBe Some(incorpInfo("#","fakeTxId-99"))
    }

    "return none for a pre incorp whitelisted regId" in {
      val res = vatregConnector.getIncorporationInfo("102", transactionID)(hc)
      await(res) shouldBe None
    }

    "return an IncorpInfo for a non-whitelisted regId" in {
      given()
          .company.isIncorporated

      val res = vatregConnector.getIncorporationInfo(nonWhitelistedRegId, transactionID)(hc)
      await(res) shouldBe Some(incorpInfo())
    }
  }

  "submitRegistration" should {

    "return success response and not submit to the backend for a whitelisted regId" in {
      val res = vatregConnector.submitRegistration("99")(hc)
      await(res) shouldBe Success
    }
    "return a success response and submit to the backend for a non-whitelisted regId" in {
      given()
        .vatScheme.isSubmittedSuccessfully(nonWhitelistedRegId)

      val res = vatregConnector.submitRegistration(nonWhitelistedRegId)(hc)
      await(res) shouldBe Success
    }
  }

  "getAckRef" should {
    "return default ackref for a whitelisted regId" in {

      val res = vatregConnector.getAckRef("99")(hc)
      await(res) shouldBe "fooBarWizzFAKEAckRef"
    }
    "return an ackref for a non-whitelisted regId" in {
      given()
        .vatScheme.has("acknowledgement-reference", JsString("fudgeAndFooAndBar"))

      val res = vatregConnector.getAckRef("1")(hc)
      await(res) shouldBe "fudgeAndFooAndBar"
    }
  }
}

