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

package controllers

import features.tradingDetails.TradingDetails
import it.fixtures.ITRegistrationFixtures
import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class TradingDetailsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with ITRegistrationFixtures {
  val companyName = "Test Company Ltd"

  "show Trading Name page" should {
    "return 200" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile()
        .s4lContainer[TradingDetails].contains(tradingDetails)
        .company.nameIs(companyName)
        .audit.writesAudit()
        .audit.writesAuditMerged()

      val response = buildClient("/trading-name").get()
      whenReady(response) { res =>
        res.status mustBe 200

        val document = Jsoup.parse(res.body)
        val elems = document.getElementById("lead-paragraph")
        elems.text must include(companyName)
      }
    }
  }
}
