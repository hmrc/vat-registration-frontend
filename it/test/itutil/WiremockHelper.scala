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
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.AffinityGroup

object WiremockHelper {
  val wiremockPort = 11111
  val wiremockHost = "localhost"
  val url = s"http://$wiremockHost:$wiremockPort"
}

trait WiremockHelper {

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
