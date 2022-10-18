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
package itutil

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.Application
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.crypto.json.JsonEncryptor
import uk.gov.hmrc.crypto.{CryptoWithKeysFromConfig, Protected}

import java.time.LocalDateTime

object WiremockHelper {
  val wiremockPort = 11111
  val wiremockHost = "localhost"
  val url = s"http://$wiremockHost:$wiremockPort"
}

trait WiremockHelper extends WiremockS4LHelper {

  import WiremockHelper._

  val wmConfig = wireMockConfig().port(wiremockPort)
  val wireMockServer = new WireMockServer(wmConfig)

  def startWiremock() = {
    configureFor(wiremockHost, wiremockPort)
    wireMockServer.start()
  }

  def stopWiremock() = wireMockServer.stop()

  def resetWiremock() = WireMock.reset()

  def stubGet(url: String, status: Integer, body: String) =
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(body)
      )
    )

  def stubPost(url: String, status: Integer, responseBody: String) =
    stubFor(post(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubPut(url: String, status: Integer, responseBody: String) =
    stubFor(put(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubPatch(url: String, status: Integer, responseBody: String) =
    stubFor(patch(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubDelete(url: String, status: Integer, responseBody: String) =
    stubFor(delete(urlMatching(url))
      .willReturn(
        aResponse().
          withStatus(status).
          withBody(responseBody)
      )
    )

  def stubAuthWithAffinity(affinity: AffinityGroup): StubMapping = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok(s"${affinity.toJson}")))
  }

  def stubAuthWithInternalId(internalId: String): StubMapping = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok(Json.obj("internalId" -> internalId).toString))
    )
  }

  def stubBankReputationCheck(valid: String): StubMapping = {
    val response = Json.obj("accountNumberIsWellFormatted" -> valid)
    stubFor(post(urlMatching("/validate/bank-details"))
      .willReturn(
        aResponse().
          withStatus(200)
          .withBody(response.toString())
      )
    )
  }

  def stubVATFetch(regId: String, uri: String, response: Option[JsObject]): StubMapping = {
    val vatRegistrationUrl = s"/vatreg/$regId/$uri"
    val (status, resp) = response.fold((404, Json.obj()))(js => (200, js))
    stubFor(get(urlMatching(vatRegistrationUrl))
      .willReturn(
        aResponse().
          withStatus(status)
          .withBody(resp.toString())
      )
    )
  }

  def stubVATFetchBankAccount(regId: String, response: Option[JsObject]): StubMapping = stubVATFetch(regId, "bank-account", response)

  def stubVATPatch(regId: String, uri: String, response: Option[JsObject]): StubMapping = {
    val vatRegistrationUrl = s"/vatreg/$regId/$uri"
    val resp = response.fold(Json.obj().toString())(_.toString())
    stubFor(patch(urlMatching(vatRegistrationUrl))
      .willReturn(
        aResponse().
          withStatus(200)
          .withBody(resp)
      )
    )
  }

  def stubVATPatchBankAccount(regId: String, response: JsObject): StubMapping = stubVATPatch(regId, "bank-account", Some(response))
}

trait WiremockS4LHelper {

  private def encryptJson(body: JsObject)(implicit app: Application): String = {
    val crypto = new CryptoWithKeysFromConfig(baseConfigKey = "json.encryption", app.configuration.underlying)
    val encryptionFormat = new JsonEncryptor[JsObject]()(crypto, implicitly)
    encryptionFormat.writes(Protected(body)).toString()
  }

  def stubS4LFetch(regId: String, key: String, response: Option[JsObject])(implicit app: Application): StubMapping = {
    val save4LaterUrl = s"/save4later/vat-registration-frontend/$regId"
    val json = response.fold(Json.obj())(js => Json.obj(key -> encryptJson(js)))

    stubFor(get(urlMatching(save4LaterUrl))
      .willReturn(
        aResponse().
          withStatus(200).
          withBody(
            Json.parse(
              s"""{
                 |  "id":"$regId",
                 |  "data": $json,
                 |  "modifiedDetails": {
                 |    "lastUpdated": ${Json.toJson(LocalDateTime.now())},
                 |    "createdAt": ${Json.toJson(LocalDateTime.now())}
                 |  }
                 |}""".stripMargin).as[JsObject].toString()
          )
      )
    )
  }

  def stubS4LSave(regId: String, key: String): StubMapping = {
    val save4LaterUrl = s"/save4later/vat-registration-frontend/$regId/data/$key"

    stubFor(put(urlMatching(save4LaterUrl))
      .willReturn(
        aResponse().
          withStatus(200).
          withBody(
            Json.parse(
            s"""{
               |  "id":"$regId",
               |  "data": {"test":"cacheMap"},
               |  "modifiedDetails": {
               |    "lastUpdated": ${Json.toJson(LocalDateTime.now())},
               |    "createdAt": ${Json.toJson(LocalDateTime.now())}
               |  }
               |}""".stripMargin).as[JsObject].toString()
          )
      )
    )
  }
}
