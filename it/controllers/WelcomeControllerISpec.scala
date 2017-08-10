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

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

import scala.concurrent.ExecutionContext.Implicits.global

class WelcomeControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  def controller: WelcomeController = app.injector.instanceOf(classOf[WelcomeController])

  "WelcomeController" must {
    "return an OK status" when {
      "user is authenticated and authorised to access the app" in {
        given()
          .user.isAuthorised
          .vatRegistrationFootprint.exists
          .corporationTaxRegistration.existsWithStatus("held")
          .company.isIncorporated

        whenReady(controller.start(request))(res => res.header.status === 200)
      }
    }
  }

}