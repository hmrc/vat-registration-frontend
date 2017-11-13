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
import it.fixtures.VatRegistrationFixture
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

class WelcomeControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with VatRegistrationFixture {

  def controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])

  "WelcomeController" must {
    "return an OK status" when {
      "user is authenticated and authorised to access the app" in {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists(Some(STARTED), Some("Vat Reg Footprint created"))
          .corporationTaxRegistration.existsWithStatus("held")
          .company.isIncorporated
          .currentProfile.setup(currentState = Some("Vat Reg Footprint created"))
          .vatScheme.contains(vatRegIncorporated.copy(lodgingOfficer = Some(lodgingOfficer.copy(ivPassed = true))))

        whenReady(controller.start(request))(res => res.header.status mustBe 200)
      }
    }
  }

}
