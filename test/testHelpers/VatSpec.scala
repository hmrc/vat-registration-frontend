/*
 * Copyright 2020 HM Revenue & Customs
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

package testHelpers

import java.time.LocalDate

import common.enums.VatRegStatus
import config.AppConfig
import fixtures.VatRegistrationFixture
import mocks.VatMocks
import models.CurrentProfile
import models.external.{CoHoRegisteredOfficeAddress, Officer, OfficerList}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsValue, Json}
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext

trait VatSpec extends PlaySpec with MockitoSugar with VatRegistrationFixture with VatMocks
  with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ex: ExecutionContext =  scala.concurrent.ExecutionContext.Implicits.global

  implicit val currentProfile: CurrentProfile = CurrentProfile(
    companyName = "Test Me",
    registrationId = testRegId,
    transactionId = "000-434-1",
    vatRegistrationStatus = VatRegStatus.draft,
    incorporationDate = Some(LocalDate.of(2017, 12, 21)),
    ivPassed = Some(true)
  )

  val dummyCacheMap: CacheMap = CacheMap("", Map.empty)

  override protected def beforeEach() {
    resetMocks()
  }

  //TODO: Complete this function to create the config
  def generateConfig(newAnalyticsToken: String                    = "",
                     newAnalyticsHost: String                     = "",
                     newReportAProblemPartialUrl: String          = "",
                     newReportAProblemNonJSUrl: String            = "",
                     newTimeoutInSeconds: String                  = "",
                     newContactFrontendPartialBaseUrl: String     = "",
                     newWhitelistedPostIncorpRegIds: Seq[String]  = Seq(),
                     newWhitelistedPreIncorpRegIds: Seq[String]   = Seq(),
                     newWhitelistedOfficersList: Seq[Officer]     = Seq(),
                     newWhitelistedCompanyName: JsValue           = Json.obj() ) = new AppConfig {
    val analyticsToken: String                      = newAnalyticsToken
    val analyticsHost: String                       = newAnalyticsHost
    val reportAProblemPartialUrl: String            = newReportAProblemPartialUrl
    val reportAProblemNonJSUrl: String              = newReportAProblemNonJSUrl
    val timeoutInSeconds: String                    = newTimeoutInSeconds
    val contactFrontendPartialBaseUrl: String       = newContactFrontendPartialBaseUrl
    val whitelistedPostIncorpRegIds: Seq[String]    = newWhitelistedPostIncorpRegIds
    val whitelistedPreIncorpRegIds: Seq[String]     = newWhitelistedPreIncorpRegIds
    val defaultCompanyName: JsValue                 = newWhitelistedCompanyName
    val defaultCohoROA: CoHoRegisteredOfficeAddress = CoHoRegisteredOfficeAddress("premises",
      "line1",
      Some("line2"),
      "locality",
      Some("UK"),
      Some("po_box"),
      Some("XX XX"),
      Some("region"))
    val defaultOfficerList: OfficerList             = OfficerList(newWhitelistedOfficersList)
  }
}
