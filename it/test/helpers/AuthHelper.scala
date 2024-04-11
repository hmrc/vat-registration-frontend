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

package helpers

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.HeaderNames
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.WSCookie
import support.{AppAndStubs, SessionCookieBaker}
import uk.gov.hmrc.crypto.{Crypted, PlainText, SymmetricCryptoFactory}
import uk.gov.hmrc.http.SessionKeys

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.StandardCharsets

trait AuthHelper {
  this: AppAndStubs =>

  private[helpers] val defaultUser = "/foo/bar"
  //  private val defaultUser = "/auth/oid/1234567890"

  val userIdKey: String = "userId"
  val tokenKey: String = "token"
  val authProviderKey: String = "ap"

  private def cookieData(additionalData: Map[String, String], userId: String = defaultUser): Map[String, String] = {
    Map(
      SessionKeys.sessionId -> sessionString,
      tokenKey -> "token",
      userIdKey -> userId,
      authProviderKey -> "GGW",
      SessionKeys.lastRequestTimestamp -> new java.util.Date().getTime.toString
    ) ++ additionalData
  }

  def getSessionCookie(additionalData: Map[String, String] = Map(), userId: String = defaultUser) = {
    SessionCookieBaker.cookieValue(cookieData(additionalData, userId))
  }

  def stubSuccessfulLogin(userId: String = defaultUser, withSignIn: Boolean = false) = {

    if (withSignIn) {
      val continueUrl = "/wibble"
      stubFor(get(urlEqualTo(s"/gg/sign-in?continue=${continueUrl}"))
        .willReturn(aResponse()
          .withStatus(303)
          .withHeader(HeaderNames.SET_COOKIE, getSessionCookie())
          .withHeader(HeaderNames.LOCATION, continueUrl)))
    }

    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok("""{}""")))
  }

  def setupSimpleAuthMocks(userId: String = defaultUser) = {
    stubFor(post(urlMatching("/write/audit"))
      .willReturn(
        aResponse().
          withStatus(204).
          withBody("""{"x":2}""")
      )
    )

    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(ok("""{}""")))
  }
}

trait SessionCookieBaker {

  val signer: CookieSigner

  val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="

  def cookieValue(sessionData: Map[String, String]) = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data.map {
        case (k, v) => URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
      }.mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes
      PlainText(signer.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }

  def getCookieData(cookie: WSCookie): Map[String, String] = {
    getCookieData(cookie.value)
  }

  def getCookieData(cookieData: String): Map[String, String] = {

    val decrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).decrypt(Crypted(cookieData)).value
    val result = decrypted.split("&")
      .map(_.split("="))
      .map { case Array(k, v) => (k, URLDecoder.decode(v, StandardCharsets.UTF_8.name())) }
      .toMap

    result
  }
}
