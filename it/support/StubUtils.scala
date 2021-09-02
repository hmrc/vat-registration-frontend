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

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.matching.UrlPathPattern
import common.enums.VatRegStatus
import itutil.IntegrationSpecBase
import models.S4LKey
import models.api.returns.Returns
import models.api.trafficmanagement.{Draft, RegistrationChannel, RegistrationInformation, VatReg}
import models.api.{SicCode, VatScheme}
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

import java.time.LocalDate

trait StubUtils {

  final class RequestHolder(var request: FakeRequest[AnyContentAsFormUrlEncoded])

  class PreconditionBuilder(implicit requestHolder: RequestHolder) {

    implicit val builder: PreconditionBuilder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String) =
      AddressStub(id, line1, line2, country, postcode)

    def postRequest(data: Map[String, String])(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      val requestWithBody = FakeRequest("POST", "/").withFormUrlEncodedBody(data.toArray: _*)
      requestHolder.request = requestWithBody
      this
    }

    def user = UserStub()

    def alfeJourney = JourneyStub()

    def vatRegistrationFootprint = VatRegistrationFootprintStub()

    def vatScheme = VatSchemeStub()

    def vatRegistration = VatRegistrationStub()

    def icl = ICL()

    def bankAccountReputation = BankAccountReputationServiceStub()

    def s4lContainer[C: S4LKey]: ViewModelStub[C] = new ViewModelStub[C]()

    def s4lContainerInScenario[C: S4LKey]: ViewModelScenarioStub[C] = new ViewModelScenarioStub[C]()

    def audit = AuditStub()

    def vrefe = VREFE()

    def s4l = S4L()

    def trafficManagement = TrafficManagementStub()
  }

  def given()(implicit requestHolder: RequestHolder): PreconditionBuilder = {
    new PreconditionBuilder()
  }

  case class VREFE()(implicit builder: PreconditionBuilder) {
    def deleteVREFESession(): PreconditionBuilder = {
      stubFor(delete(urlMatching("/internal/1/delete-session")).willReturn(ok))
      builder
    }
  }

  trait KeystoreStub {
    def stubKeystorePut(key: String, data: String): MappingBuilder = {
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
        ))
    }

    def stubKeystoreGet(key: String, data: String): MappingBuilder =
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
        ))

    def deleteKeystore(): MappingBuilder = {
      delete(urlMatching("/keystore/vat-registration-frontend/session-[a-z0-9-]+"))
        .willReturn(ok)
    }
  }

  object S4LStub extends IntegrationSpecBase {

    import uk.gov.hmrc.crypto._
    import uk.gov.hmrc.crypto.json.JsonEncryptor

    implicit lazy val jsonCrypto = new ApplicationCrypto(app.configuration.underlying).JsonCrypto
    implicit lazy val encryptionFormat = new JsonEncryptor[JsValue]()

    def stubS4LGetNoAux(key: String, data: String): MappingBuilder = {
      val s4lData = Json.parse(data).as[JsValue]
      val encData = encryptionFormat.writes(Protected(s4lData)).as[JsString]

      val json =
        s"""
           |{
           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
           |  "data": {
           |    "$key": $encData
           |  },
           |  "id": "1",
           |  "modifiedDetails": {
           |    "createdAt": { "$$date": 1502097615710 },
           |    "lastUpdated": { "$$date": 1502189409725 }
           |  }
           |}
            """.stripMargin

      get(urlPathMatching("/save4later/vat-registration-frontend/1"))
        .willReturn(ok(
          json
        ))
    }

    def stubS4LPut(key: String, data: String): MappingBuilder = {
      val s4lData = Json.parse(data).as[JsValue]
      val encData = encryptionFormat.writes(Protected(s4lData)).as[JsString]

      put(urlPathMatching(s"/save4later/vat-registration-frontend/1/data/$key"))
        .willReturn(ok(
          s"""
             |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
             |    "data": { "$key": $encData },
             |    "id": "1",
             |    "modifiedDetails": {
             |      "createdAt": { "$$date": 1502265526026 },
             |      "lastUpdated": { "$$date": 1502265526026 }}}
          """.stripMargin
        ))
    }


    def stubS4LGet[C, T](t: T)(implicit key: S4LKey[C], fmt: Writes[T]): MappingBuilder = {
      val s4lData = Json.toJson(t)
      val encData = encryptionFormat.writes(Protected(s4lData)).as[JsString]

      get(urlPathMatching("/save4later/vat-registration-frontend/1"))
        .willReturn(ok(
          s"""
             |{
             |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
             |  "data": {
             |    "${key.key}": $encData
             |  },
             |  "id": "1",
             |  "modifiedDetails": {
             |    "createdAt": { "$$date": 1502097615710 },
             |    "lastUpdated": { "$$date": 1502189409725 }
             |  }
             |}
            """.stripMargin
        ))
    }

    def stubS4LGetNothing(): MappingBuilder =
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
        ))

    def stubS4LClear(): MappingBuilder =
      delete(urlPathMatching("/save4later/vat-registration-frontend/1")).willReturn(ok(""))
  }

  case class S4L(scenario: String = "S4L Scenario")(implicit builder: PreconditionBuilder) {
    def contains(key: String, data: String, currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioGET = S4LStub.stubS4LGetNoAux(key, data).inScenario(scenario)
      val mappingBuilderGET = currentState.fold(mappingBuilderScenarioGET)(mappingBuilderScenarioGET.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderGET)(mappingBuilderGET.willSetStateTo))
      builder
    }

    def isUpdatedWith(key: String, data: String, currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioPUT = S4LStub.stubS4LPut(key, data).inScenario(scenario)
      val mappingBuilderPUT = currentState.fold(mappingBuilderScenarioPUT)(mappingBuilderScenarioPUT.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderPUT)(mappingBuilderPUT.willSetStateTo))
      builder
    }

    def isEmpty(currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioGET = S4LStub.stubS4LGetNothing().inScenario(scenario)
      val mappingBuilderGET = currentState.fold(mappingBuilderScenarioGET)(mappingBuilderScenarioGET.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderGET)(mappingBuilderGET.willSetStateTo))
      builder
    }

    def cleared(currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioDELETE = S4LStub.stubS4LClear().inScenario(scenario)
      val mappingBuilderDELETE = currentState.fold(mappingBuilderScenarioDELETE)(mappingBuilderScenarioDELETE.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderDELETE)(mappingBuilderDELETE.willSetStateTo))
      builder
    }
  }

  @deprecated("please change the types on this once all refactoring has been completed, both should be same type instead of C & T")
  class ViewModelStub[C]()(implicit builder: PreconditionBuilder, s4LKey: S4LKey[C]) {

    def contains[T](t: T)(implicit fmt: Writes[T]): PreconditionBuilder = {
      stubFor(S4LStub.stubS4LGet[C, T](t))
      builder
    }

    def isUpdatedWith(t: C)(implicit key: S4LKey[C], fmt: Writes[C]): PreconditionBuilder = {
      stubFor(S4LStub.stubS4LPut(key.key, fmt.writes(t).toString()))
      builder
    }

    def isEmpty: PreconditionBuilder = {
      stubFor(S4LStub.stubS4LGetNothing())
      builder
    }

    def cleared: PreconditionBuilder = {
      stubFor(S4LStub.stubS4LClear())
      builder
    }

    def clearedByKey(implicit key: S4LKey[C]): PreconditionBuilder = {
      stubFor(S4LStub.stubS4LPut(key.key, Json.obj().toString()))
      builder
    }
  }

  class ViewModelScenarioStub[C](scenario: String = "S4L Scenario")
                                (implicit builder: PreconditionBuilder, s4LKey: S4LKey[C]) extends ViewModelStub {

    def contains[T](t: T, currentState: Option[String] = None, nextState: Option[String] = None)
                   (implicit fmt: Format[T]): PreconditionBuilder = {
      val mappingBuilderScenarioGET = S4LStub.stubS4LGet[C, T](t).inScenario(scenario)
      val mappingBuilderGET = currentState.fold(mappingBuilderScenarioGET)(mappingBuilderScenarioGET.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderGET)(mappingBuilderGET.willSetStateTo))
      builder
    }

    def isUpdatedWith[T](t: T, currentState: Option[String] = None, nextState: Option[String] = None)
                        (implicit key: S4LKey[C], fmt: Format[T]): PreconditionBuilder = {
      val mappingBuilderScenarioPUT = S4LStub.stubS4LPut(key.key, fmt.writes(t).toString()).inScenario(scenario)
      val mappingBuilderPUT = currentState.fold(mappingBuilderScenarioPUT)(mappingBuilderScenarioPUT.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderPUT)(mappingBuilderPUT.willSetStateTo))
      builder
    }

    def isEmpty(currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioGET = S4LStub.stubS4LGetNothing().inScenario(scenario)
      val mappingBuilderGET = currentState.fold(mappingBuilderScenarioGET)(mappingBuilderScenarioGET.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderGET)(mappingBuilderGET.willSetStateTo))
      builder
    }

    def cleared(currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      val mappingBuilderScenarioDELETE = S4LStub.stubS4LClear().inScenario(scenario)
      val mappingBuilderDELETE = currentState.fold(mappingBuilderScenarioDELETE)(mappingBuilderScenarioDELETE.whenScenarioStateIs)

      stubFor(nextState.fold(mappingBuilderDELETE)(mappingBuilderDELETE.willSetStateTo))
      builder
    }
  }

  case class VatRegistrationStub()(implicit builder: PreconditionBuilder) {
    def threshold(url: String, threshold: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(url))
          .willReturn(ok(
            s"""
               |{
               |  "taxable-threshold":"$threshold",
               |  "since":"2018-1-1"
               |}
             """.stripMargin
          ))
      )
      builder
    }

    def status(url: String, status: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(url))
          .willReturn(ok(
            s"""
               |{
               |  "status":"$status"
               |}
             """.stripMargin
          ))
      )
      builder
    }

    def submit(url: String): PreconditionBuilder = {
      stubFor(
        put(urlPathEqualTo(url))
          .willReturn(ok())
      )
      builder
    }

    def acknowledgementReference(regId: String, ackRef: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/$regId/acknowledgement-reference"))
          .willReturn(ok(ackRef))
      )
      builder
    }

    def storesNrsPayload(regId: String): PreconditionBuilder = {
      stubFor(
        patch(urlPathEqualTo(s"/vatreg/$regId/nrs-payload"))
          .willReturn(ok())
      )
      builder
    }

    def honestyDeclaration(regId: String, honestyDeclaration: String): PreconditionBuilder = {
      stubFor(
        patch(urlPathEqualTo(s"/vatreg/$regId/honesty-declaration"))
          .willReturn(ok(honestyDeclaration))
      )
      builder
    }

    def insertScheme(body: String): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/insert-s4l-scheme"))
          .willReturn(ok().withBody(body))
      )
      builder
    }

    def storesReturns(regId: String, returns: Returns): PreconditionBuilder = {
      stubFor(
        patch(urlPathEqualTo(s"/vatreg/$regId/returns"))
          .willReturn(ok().withBody(Json.toJson(returns).toString()))
      )
      builder
    }
  }

  case class ICL()(implicit builder: PreconditionBuilder, requestHolder: RequestHolder) {
    def setup(): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/internal/initialise-journey"))
          .willReturn(ok(
            s"""
               |{
               |  "fetchResultsUri" : "fetch",
               |  "journeyStartUri" : "journeyStart"
               |}
             """.stripMargin
          )))

      builder
    }

    def fetchResults(sicCodeList: List[SicCode]): PreconditionBuilder = {
      val sicJsArray = Json.toJson(sicCodeList).as[JsArray]

      stubFor(
        get(urlPathMatching("/fetch-results"))
          .willReturn(ok(
            s"""
               |{
               |  "sicCodes": $sicJsArray
               |}
             """.stripMargin
          )))

      builder
    }
  }

  case class CurrentProfile()(implicit builder: PreconditionBuilder, requestHolder: RequestHolder) {
    def setup(status: VatRegStatus.Value = VatRegStatus.draft, currentState: Option[String] = None, nextState: Option[String] = None): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/incorporation-information/000-431-TEST/company-profile"))
          .willReturn(ok(
            s"""{ "company_name": "testingCompanyName" }"""
          )))

      stubFor(get(urlPathEqualTo("/vatreg/1/status")).willReturn(ok(
        s"""{"status": "${status.toString}"}"""
      )))

      stubFor(
        get(urlPathEqualTo("/vatreg/incorporation-information/000-431-TEST"))
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
               |    "transactionId": "000-431-TEST"
               |  }
               |}
             """.stripMargin
          )))

      builder
    }
  }


  case class VatSchemeStub()(implicit builder: PreconditionBuilder) {

    def isBlank: PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/get-scheme"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" , "status" : "draft"}"""
          )))
      builder
    }

    def doesNotExistForKey(blockKey: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo(s"/vatreg/1/$blockKey"))
          .willReturn(notFound()))
      builder
    }

    def isNotUpdatedWith[T](t: T, statusCode: Int = 500)(implicit tFmt: Format[T]) = {
      stubFor(
        patch(urlPathMatching(s"/vatreg/1/.*"))
          .willReturn(aResponse().withStatus(statusCode).withBody(tFmt.writes(t).toString())))
      builder
    }

    def isUpdatedWith[T](t: T)(implicit tFmt: Writes[T]) = {
      stubFor(
        patch(urlPathMatching(s"/vatreg/1/.*"))
          .willReturn(aResponse().withStatus(202).withBody(tFmt.writes(t).toString())))
      builder
    }

    def isUpdatedWithPartner[T](t: T)(implicit tFmt: Format[T]): PreconditionBuilder = {
      stubFor(
        put(urlPathMatching(s"/vatreg/1/partners/.*"))
          .willReturn(aResponse().withStatus(CREATED).withBody(tFmt.writes(t).toString())))
      builder
    }

    def contains(vatReg: VatScheme): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo("/vatreg/1/get-scheme")).willReturn(ok(Json.toJson(vatReg).toString)))
      builder
    }


    def has(key: String, data: JsValue): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/1/$key")).willReturn(ok(data.toString())))
      builder
    }

    def doesNotHave(blockKey: String): PreconditionBuilder = {
      stubFor(get(urlPathEqualTo(s"/vatreg/1/$blockKey")).willReturn(noContent()))
      builder
    }

    def deleted: PreconditionBuilder = {
      stubFor(delete(urlPathEqualTo("/vatreg/1/delete-scheme")).willReturn(ok("")))
      builder
    }

    def patched(block: String, json: JsValue) = {
      stubFor(
        patch(urlPathMatching(s"/vatreg/1/$block"))
          .willReturn(aResponse().withStatus(202).withBody(json.toString)))
      builder
    }

    def isSubmittedSuccessfully(regId: String = "1"): PreconditionBuilder = {
      stubFor(
        put(urlPathMatching(s"/vatreg/$regId/submit-registration"))
          .willReturn(aResponse().withStatus(200).withBody("fooBar")))
      builder
    }

    def regStatus(status: String): PreconditionBuilder = {
      stubFor(
        get(urlPathEqualTo("/vatreg/1/status"))
          .willReturn(ok(
            s"""{"status": "${status}"}"""
          ))
      )

      builder
    }

  }

  case class VatRegistrationFootprintStub()(implicit builder: PreconditionBuilder) {

    def exists(): PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(ok(
            s"""{ "registrationId" : "1" , "status" : "draft"}"""
          )))

      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(
        post(urlPathEqualTo("/vatreg/new"))
          .willReturn(serverError()))

      builder
    }
  }

  case class UserStub()(implicit builder: PreconditionBuilder) {

    def isAuthorised(implicit requestHolder: RequestHolder): PreconditionBuilder = {
      requestHolder.request = SessionCookieBaker.requestWithSession(requestHolder.request, "anyUserId")

      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(ok(Json.obj(
            "internalId" -> "1",
            "affinityGroup" -> Organisation.toString
          ).toString)))

      builder
    }

    def isNotAuthorised = {
      stubFor(
        post(urlPathEqualTo("/auth/authorise"))
          .willReturn(unauthorized()))

      builder
    }
  }

  case class JourneyStub()(implicit builder: PreconditionBuilder) {

    val journeyInitUrl: UrlPathPattern = urlPathMatching(s".*/api/v2/init")

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

    val confirmedAddressPath = s""".*/api/v2/confirmed[?]id=$id"""

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
               |      "code": "$country",
               |      "name": "United Kingdom"
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

  case class AuditStub()(implicit builder: PreconditionBuilder) {
    def writesAudit(status: Int = 204) = {
      stubFor(post(urlMatching("/write/audit"))
        .willReturn(
          aResponse().
            withStatus(status).
            withBody("""{"x":2}""")
        )
      )
      builder
    }

    def writesAuditMerged(status: Int = 204) = {
      stubFor(post(urlMatching("/write/audit/merged"))
        .willReturn(
          aResponse().
            withStatus(status).
            withBody("""{"x":2}""")
        )
      )
      builder
    }

    def failsToWriteAudit() = {
      writesAudit(404)
    }
  }

  case class BankAccountReputationServiceStub()(implicit builder: PreconditionBuilder) {
    def passes: PreconditionBuilder = {
      stubFor(post(urlMatching("/v2/validateBankDetails"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            s"""
               |{
               |  "accountNumberWithSortCodeIsValid": "yes",
               |  "nonStandardAccountDetailsRequiredForBacs": "no"
               |}
              """.stripMargin)
        ))

      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(post(urlMatching("/v2/validateBankDetails"))
        .willReturn(
          aResponse().withStatus(200).withBody(
            s"""
               |{
               |  "accountNumberWithSortCodeIsValid": "no",
               |  "nonStandardAccountDetailsRequiredForBacs": "no"
               |}
              """.stripMargin)
        ))
      builder
    }

    def isDown: PreconditionBuilder = {
      stubFor(post(urlMatching("/v2/validateBankDetails"))
        .willReturn(
          serverError()
        ))
      builder
    }
  }

  case class TrafficManagementStub()(implicit builder: PreconditionBuilder) {
    def passes(channel: RegistrationChannel = VatReg): PreconditionBuilder = {
      stubFor(get(urlMatching("/vatreg/traffic-management/reg-info"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(Json.toJson(
              RegistrationInformation("1", "1", Draft, Some(LocalDate.now()), channel)
            ).toString())
        ))
      builder
    }

    def fails: PreconditionBuilder = {
      stubFor(get(urlMatching("/vatreg/traffic-management/reg-info"))
        .willReturn(
          aResponse()
            .withStatus(204)
        ))
      builder
    }

    def isCleared: PreconditionBuilder = {
      stubFor(delete(urlMatching("/vatreg/traffic-management/reg-info/clear"))
        .willReturn(
          aResponse()
            .withStatus(204)
        ))
      builder
    }

    def failsToClear: PreconditionBuilder = {
      stubFor(delete(urlMatching("/vatreg/traffic-management/reg-info/clear"))
        .willReturn(
          aResponse()
            .withStatus(400)
        ))
      builder
    }
  }

}