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

import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import common.enums.IVResult
import helpers.RequestsFinder
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HeaderNames
import support.AppAndStubs

class IdentityVerificationControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with RequestsFinder {
  "GET Complete IV Journey" should {
    "redirect to 'Have you ever changed your name?' page" in {
      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()

      val response = buildClient(s"/ivComplete").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.vatLodgingOfficer.routes.FormerNameController.show().url)
      }
    }
  }

  "GET Failed IV Journey" should {
    "redirect to correct error page" in {
      val journeyId = "12345"

      given()
        .user.isAuthorised
        .currentProfile.withProfile(Some(STARTED), Some("Current Profile"))
        .audit.writesAudit()
        .iv.outcome(journeyId, IVResult.Timeout)

      val response = buildClient(s"/ivFailure?journeyId=$journeyId").get()
      whenReady(response) { res =>
        res.status mustBe 303
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.iv.routes.IdentityVerificationController.timeoutIV().url)
      }
    }
  }
}
