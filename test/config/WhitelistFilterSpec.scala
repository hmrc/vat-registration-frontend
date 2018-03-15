/*
 * Copyright 2018 HM Revenue & Customs
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

package config

import java.util.Base64

import org.scalatest.TestData
import org.scalatestplus.play.{OneAppPerTest, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Environment, Mode}

class WhitelistFilterSpec extends PlaySpec with OneAppPerTest {

  override def newAppForTest(td: TestData): Application = new GuiceApplicationBuilder()
    .in(Environment(new java.io.File("."), classOf[FakeApplication].getClassLoader, Mode.Test))
    .global(ProductionFrontendGlobal)
    .configure(Map(
      "whitelist-excluded" -> Base64.getEncoder.encodeToString("/ping/ping,/healthcheck".getBytes),
      "whitelist" -> Base64.getEncoder.encodeToString("whitelistIP".getBytes)
    ))
    .build

  "FrontendAppConfig" must {
    "return a valid config item" when {
      "the whitelist exclusion paths are requested" in {
        FrontendAppConfig.whitelistExcluded mustBe Seq("/ping/ping", "/healthcheck")
      }
      "the whitelist IPs are requested" in {
        FrontendAppConfig.whitelist mustBe Seq("whitelistIP")
      }
    }
  }

  "ProductionFrontendGlobal" must {
    "let requests passing" when {
      "coming from an IP in the white list must work as normal" in {
        val request = FakeRequest(GET, "/register-for-vat").withHeaders("True-Client-IP" -> "whitelistIP")
        val Some(result) = route(app, request)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some("/register-for-vat/before-you-register-for-vat")
      }

      "coming from a IP NOT in the white-list and not with a white-listed path must be redirected" in {
        val request = FakeRequest(GET, "/register-for-vat").withHeaders("True-Client-IP" -> "nonWhitelistIP")
        val Some(result) = route(app, request)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some("https://www.tax.service.gov.uk/outage-register-for-vat")
      }

      "coming from an IP NOT in the white-list, but with a white-listed path must work as normal" in {
        val request = FakeRequest(GET, "/ping/ping").withHeaders("True-Client-IP" -> "nonWhitelistIP")
        val Some(result) = route(app, request)

        status(result) mustBe OK
      }

      "coming without an IP header must fail" in {
        val request = FakeRequest(GET, "/register-for-vat")
        val Some(result) = route(app, request)

        status(result) mustBe NOT_IMPLEMENTED
      }
    }
  }
}
