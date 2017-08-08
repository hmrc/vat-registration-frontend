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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest

trait WiremockAware {
  def wiremockBaseUrl: String
}

trait StubUtils {
  me: StartAndStopWireMock =>

  final class RequestHolder(var request: FakeRequest[AnyContentAsEmpty.type])

  class PreconditionBuilder {

    implicit val builder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String) =
      AddressStub(id, line1, line2, country, postcode)

    def journey(id: String) = JourneyStub(id)

    def user(id: String) = UserStub(id)

    def vatRegistrationFootprint(id: String) = VatRegistrationFootprintStub(id)

    def corporationTaxRegistration(regId: String) = CorporationTaxRegistrationStub(regId)

    def company(txId: String) = IncorporationStub(txId)

  }

  def given() = {
    new PreconditionBuilder()
  }

  trait KeystoreStub {
    def stubKeystorePut(key: String, data: String) =
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
  }

  case class IncorporationStub
  (txId: String)
  (implicit builder: PreconditionBuilder) {

    def isIncorporated(): PreconditionBuilder = {
      stubFor(
        get(urlPathMatching("/keystore/vat-registration-frontend/session-[a-z0-9-]+"))
          .willReturn(ok(
            s"""
               |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
               |    "data": {
               |     "CompanyProfile" : {
               |       "status" : "held",
               |       "confirmationReferences" : {
               |         "transaction-id" : "000-434-$txId"
               |    } } },
               |    "id": "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1",
               |    "modifiedDetails": {
               |      "createdAt": { "$$date": 1502265526026 },
               |      "lastUpdated": { "$$date": 1502265526026 }}}
            """.stripMargin
          )))

      stubFor(
        get(urlPathEqualTo(s"/vatreg/incorporation-information/000-434-$txId"))
          .willReturn(ok(
            s"""
               |{
               |  "statusEvent": {
               |    "crn": "90000001",
               |    "incorporationDate": "2016-08-05",
               |    "status": "accepted"
               |  },
               |  "subscription": {
               |    "callbackUrl": "http://localhost:9896/TODO-CHANGE-THIS",
               |    "regime": "vat",
               |    "subscriber": "scrs",
               |    "transactionId": "000-434-$txId"
               |  }
               |}
             """.stripMargin
          ))
      )
      builder
    }

    def incorporationStatusNotKnown(): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/incorporation-information/000-434-$txId"))
          .willReturn(notFound().withBody(
            s"""
               |{
               |    "errorCode": 404,
               |    "errorMessage": "Incorporation Status not known. A subscription has been setup"
               |}
             """.stripMargin
          )))
      builder
    }
  }


  case class CorporationTaxRegistrationStub
  (regId: String)
  (implicit builder: PreconditionBuilder) {

    def existsWithStatus(status: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/incorporation-frontend-stubs/$regId/corporation-tax-registration"))
          .willReturn(ok(
            s"""{ "confirmationReferences": { "transaction-id": "000-434-$regId" }, "status": "$status" }"""
          )))
      builder
    }

  }

  case class VatRegistrationFootprintStub
  (vatSchemeId: String)
  (implicit builder: PreconditionBuilder) extends KeystoreStub {
    def exists(): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(ok(
            s"""{ "registrationId" : "$vatSchemeId" }"""
          )))

      stubKeystorePut("RegistrationId",      """{ "foo": "Bar" }""")
      stubKeystorePut("CompanyProfile",      """{ "foo": "Bar" }""")
      stubKeystorePut("incorporationStatus", """{ "foo": "Bar" }""")

      builder
    }
  }

  case class UserStub
  (userId: String)
  (implicit builder: PreconditionBuilder) extends SessionBuilder {

    def isAuthenticatedAndAuthorised()(implicit requestHolder: RequestHolder) = {
      requestHolder.request = requestWithSession(userId)
      stubFor(
        get(urlPathEqualTo("/auth/authority"))
          .willReturn(ok(
            s"""
               |{
               |  "uri":"$userId",
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

    def initialisedSuccessfully() = {
      stubFor(
        post(urlMatching(s""".*/api/init/$journeyId"""))
          .willReturn(aResponse.withStatus(202).withHeader("Location", "continueUrl")))
      builder
    }

    def failedToInitialise() = {
      stubFor(
        post(urlMatching(s""".*/api/init/$journeyId"""))
          .willReturn(ok()))
      builder
    }

  }

  case class AddressStub
  (id: String, line1: String, line2: String, country: String, postcode: String)
  (implicit builder: PreconditionBuilder) {

    val confirmedAddressPath = s""".*/api/confirmed[?]id=$id"""

    def isFound() = {
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

    def isNotFound() = {
      stubFor(
        get(urlMatching(confirmedAddressPath))
          .willReturn(notFound()))
      builder
    }
  }

}