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


import org.scalatest.{Suite, TestSuite}
import org.scalatestplus.play.OneServerPerSuite
import play.api.test.FakeApplication
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.it.Port

trait AppAndStubs extends StartAndStopWireMock with StubUtils with OneServerPerSuite {
  me: Suite with TestSuite =>

  implicit val hc = HeaderCarrier()
  implicit val portNum = port

  override lazy val port: Int = Port.randomAvailable

  override implicit lazy val app: FakeApplication = FakeApplication(
    //override app config here, chaning hosts and ports to point app at Wiremock
    additionalConfiguration = Map(
      "microservice.services.address-lookup-frontend.host" -> wiremockHost,
      "microservice.services.address-lookup-frontend.port" -> wiremockPort
    )
  )

}

