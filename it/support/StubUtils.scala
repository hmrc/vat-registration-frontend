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

package support

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.S4LKey
import play.api.libs.json.Format
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes

trait StubUtils {
  me: StartAndStopWireMock =>

  final class RequestHolder(var request: FakeRequest[AnyContentAsEmpty.type])

  class PreconditionBuilder {

    implicit val builder: PreconditionBuilder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String) =
      AddressStub(id, line1, line2, country, postcode)

    def journey(id: String) = JourneyStub(id)

    def user = UserStub()

    def vatRegistrationFootprint = VatRegistrationFootprintStub()

    def vatScheme = VatSchemeStub()

    def corporationTaxRegistration = CorporationTaxRegistrationStub()

    def company = IncorporationStub()

    def s4lContainer[C: S4LKey]: ViewModelStub[C] = new ViewModelStub[C]()

  }

  def given(): PreconditionBuilder = {
    new PreconditionBuilder()
  }

  trait KeystoreStub {
    def stubKeystorePut(key: String, data: String): StubMapping =
      stubFor(
        put(urlPathMatching(s"/keystore/vat-registration-frontend/session-[a-z0-9-]+/data/$key"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": $data },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

    def stubKeystoreGet(key: String, data: String): StubMapping =
      stubFor(
        get(urlPathMatching("/keystore/vat-registration-frontend/session-[a-z0-9-]+"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": $data },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

  }

  trait S4LStub {

    import uk.gov.hmrc.crypto._

    //TODO get the json.encryption.key config value from application.conf
    val crypto: CompositeSymmetricCrypto = aes("fqpLDZ4sumDsekHkeEBlCA==", Seq.empty)

    def decrypt(encData: String): String = crypto.decrypt(Crypted(encData)).value

    def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value


    def stubS4LPut(key: String, data: String): StubMapping =
      stubFor(
        put(urlPathMatching(s"/save4later/vat-registration-frontend/1/data/$key"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": { "$key": "${encrypt(data)}" },
               |    "id": "1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

    def stubS4LGet[C, T](t: T)(implicit key: S4LKey[C], fmt: Format[T]): StubMapping =
      stubFor(
        get(urlPathMatching("/save4later/vat-registration-frontend/1"))
          .willReturn(ok(
            s"""
               |{
               |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
               |  "data": {
               |    "${key.key}": "${encrypt(fmt.writes(t).toString())}"
               |  },
               |  "id": "1",
               |  "modifiedDetails": {
               |    "createdAt": { "$$date": 1502097615710 },
               |    "lastUpdated": { "$$date": 1502189409725 }
               |  }
               |}
            """.stripMargin
          )))

    def stubS4LGetNothing(): StubMapping =
      stubFor(
        get(urlPathMatching("/save4later/vat-registration-frontend/1"))
          .willReturn(ok(
            s"""
               |{
               |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
               |  "data": {},
               |  "id": "1",
               |  "modifiedDetails": {
               |    "createdAt": { "$$date": 1502097615710 },
               |    "lastUpdated": { "$$date": 1502189409725 }
               |  }
               |}
            """.stripMargin
          )))

  }


  class ViewModelStub[C]()(implicit builder: PreconditionBuilder, s4LKey: S4LKey[C]) extends S4LStub with KeystoreStub {

    def contains[T](t: T)(implicit fmt: Format[T]): PreconditionBuilder = {
      stubKeystoreGet("RegistrationId", "\"1\"")
      stubS4LGet[C, T](t)
      builder
    }

    def isEmpty: PreconditionBuilder = {
      stubKeystoreGet("RegistrationId", "\"1\"")
      stubS4LGetNothing()
      builder
    }

  }


  case class IncorporationStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def isIncorporated: PreconditionBuilder = {

      stubKeystoreGet(
        "CompanyProfile",
        """{ "status" : "held",
          |  "confirmationReferences" : {
          |    "transaction-id" : "000-434-1"
          |}}""".stripMargin)

      stubFor(
        get(urlPathEqualTo("/vatreg/incorporation-information/000-434-1"))
          .willReturn(ok(
            s"""
               |{
               |  "statusEvent": {
               |    "crn": "90000001",
               |    "incorporationDate": "2016-08-05",
               |    "status": "accepted"
               |  },
               |  "subscription": {
               |    "callbackUrl": "http://localhost:9896/callbackUrl",
               |    "regime": "vat",
               |    "subscriber": "scrs",
               |    "transactionId": "000-434-1"
               |  }
               |}
             """.stripMargin
          ))
      )
      builder
    }

    def incorporationStatusNotKnown(): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/incorporation-information/000-434-1"))
          .willReturn(notFound().withBody(
            s"""
               |{
               |  "errorCode": 404,
               |  "errorMessage": "Incorporation Status not known. A subscription has been setup"
               |}
             """.stripMargin
          )))
      builder
    }
  }


  case class CorporationTaxRegistrationStub
  ()
  (implicit builder: PreconditionBuilder) {

    def existsWithStatus(status: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/incorporation-frontend-stubs/1/corporation-tax-registration"))
          .willReturn(ok(
            s"""{ "confirmationReferences": { "transaction-id": "000-434-1" }, "status": "$status" }"""
          )))
      builder
    }

  }


  case class VatSchemeStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def isBlank: PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/get-scheme"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" }"""
          )))
      builder
    }

  }

  case class VatRegistrationFootprintStub
  ()
  (implicit builder: PreconditionBuilder) extends KeystoreStub {

    def exists: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" }"""
          )))

      stubKeystorePut("RegistrationId", "{}")
      stubKeystorePut("CompanyProfile", "{}")
      stubKeystorePut("incorporationStatus", "{}")

      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(serverError()))

      builder
    }
  }

  case class UserStub
  ()
  (implicit builder: PreconditionBuilder) extends SessionBuilder {

    def isAuthorised(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      requestHolder.request = requestWithSession("anyUserId")
      stubFor(
        get(urlPathEqualTo("/auth/authority"))
          .willReturn(ok(
            s"""
               |{
               |  "uri":"anyUserId",
               |  "loggedInAt": "2014-06-09T14:57:09.522Z",
               |  "previouslyLoggedInAt": "2014-06-09T14:48:24.841Z",
               |  "credentials": {"gatewayId":"xxx2"},
               |  "accounts": {},
               |  "levelOfAssurance": "2",
               |  "confidenceLevel" : 50,
               |  "credentialStrength": "strong",
               |  "legacyOid": "1234567890",
               |  "userDetailsLink": "http://localhost:11111/auth/userDetails",
               |  "ids": "/auth/ids"
               |}""".stripMargin
          )))
      builder
    }

  }

  case class JourneyStub
  (journeyId: String)
  (implicit builder: PreconditionBuilder) {

    val journeyInitUrl: UrlPathPattern = urlPathMatching(s".*/api/init/$journeyId")

    def initialisedSuccessfully(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse.withStatus(202).withHeader("Location", "continueUrl")))
      builder
    }

    def notInitialisedAsExpected(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(aResponse().withStatus(202))) // a 202 _without_ Location header
      builder
    }

    def failedToInitialise(): PreconditionBuilder = {
      stubFor(post(journeyInitUrl).willReturn(serverError()))
      builder
    }

  }

  case class AddressStub
  (id: String, line1: String, line2: String, country: String, postcode: String)
  (implicit builder: PreconditionBuilder) {

    val confirmedAddressPath = s""".*/api/confirmed[?]id=$id"""

    def isFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(ok(
            s"""
               |{
               |  "auditRef": "$id",
               |  "id": "GB990091234520",
               |  "address": {
               |    "country": {
               |      "code": "GB",
               |      "name": "$country"
               |    },
               |    "lines": [
               |      "$line1",
               |      "$line2"
               |    ],
               |    "postcode": "$postcode"
               |  }
               |}
         """.stripMargin
          )))
      builder
    }

    def isNotFound: PreconditionBuilder = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(notFound()))
      builder
    }
  }

}
