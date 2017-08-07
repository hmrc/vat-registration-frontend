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


trait WiremockAware {
  def wiremockBaseUrl: String
}

trait StubUtils {
  me: StartAndStopWireMock =>

  class PreconditionBuilder {

    implicit val builder = this

    def address(id: String, line1: String, line2: String, country: String, postcode: String) =
      AddressStub(id, line1, line2, country, postcode)

    def journey(id: String) = JourneyStub(id)

  }

  def given() = {
    new PreconditionBuilder()
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

    def isFound() = {
      stubFor(
        get(urlMatching(s""".*/api/confirmed[?]id=$id"""))
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
        get(urlMatching(s""".*/api/confirmed[?]id=$id"""))
          .willReturn(notFound()))
      builder
    }
  }

}