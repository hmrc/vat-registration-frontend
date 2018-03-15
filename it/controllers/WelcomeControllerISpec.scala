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
import it.fixtures.ITRegistrationFixtures
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import support.AppAndStubs
import utils.VATRegFeatureSwitch

class WelcomeControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures with ITRegistrationFixtures {

  def controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])
  val featureSwitch: VATRegFeatureSwitch = app.injector.instanceOf[VATRegFeatureSwitch]

  "WelcomeController" must {
    "return an OK status" when {
      "user is authenticated and authorised to access the app" in {
        featureSwitch.manager.enable(featureSwitch.useCrStubbed)

        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists(Some(STARTED), Some("Vat Reg Footprint created"))
          .businessRegistration.exists()
          .corporationTaxRegistration.existsWithStatus("held")
          .company.isIncorporated
          .currentProfile.setup(currentState = Some("Vat Reg Footprint created"))
          .vatScheme.contains(vatRegIncorporated)
          .vatScheme.has("officer", Json.obj())
          .audit.writesAuditMerged()

        whenReady(controller.start(request))(res => res.header.status mustBe 200)
      }
    }

    "return a redirect to CR post sign in" when {
      "the user has no business registration" in {
        featureSwitch.manager.enable(featureSwitch.useCrStubbed)

        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists(Some(STARTED), Some("Vat Reg Footprint created"))
          .businessRegistration.fails
          .corporationTaxRegistration.existsWithStatus("held")
          .audit.writesAuditMerged()

        whenReady(controller.start(request))(res => res.header.status mustBe 303)
      }

      "the user has a BR but an unfinished CT registration" in {
        featureSwitch.manager.enable(featureSwitch.useCrStubbed)

        given()
          .user.isAuthorised
          .businessRegistration.exists()
          .corporationTaxRegistration.existsWithStatus("draft")
          .vatRegistrationFootprint.exists(Some(STARTED), Some("Vat Reg Footprint created"))
          .audit.writesAuditMerged()

        whenReady(controller.start(request))(res => res.header.status mustBe 303)
      }
    }
  }

}
