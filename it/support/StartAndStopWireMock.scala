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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{configureFor, reset}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import uk.gov.hmrc.play.it.Port.randomAvailable

import scala.collection.JavaConversions._

trait StartAndStopWireMock extends BeforeAndAfterEach with BeforeAndAfterAll {
  self: Suite =>

  protected val wiremockPort = randomAvailable
  protected val wiremockHost = "localhost"
  protected val wiremockBaseUrl: String = s"http://$wiremockHost:$wiremockPort"
  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    configureFor(wiremockHost, wiremockPort)
  }

  override def beforeEach() = {
    reset()
  }

  override def afterEach(): Unit = {
    println("===== REQUESTS =====")
    wireMockServer.getAllServeEvents.toList
      .sortBy(_.getRequest.getLoggedDate)
      .map(_.getRequest).map(r => s"${r.getLoggedDate.toInstant.toEpochMilli}\t${r.getMethod}\t${r.getUrl}")
      .foreach(println)
    println("===== END =====")
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }
}
