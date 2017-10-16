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

import controllers.frs.JoinFrsController
import models.S4LFlatRateScheme
import models.api.VatFlatRateScheme
import models.view.frs.JoinFrsView
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.Logger
import support.AppAndStubs

import scala.concurrent.ExecutionContext.Implicits.global

class JoinFrsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  def controller: JoinFrsController = app.injector.instanceOf(classOf[JoinFrsController])

  "accessing the Join FRS page" must {
    "return an OK status" when {
      "a view is in Save 4 Later" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer[S4LFlatRateScheme].contains(JoinFrsView(selection = true))

        whenReady(controller.show(request)) { res =>
          res.header.status mustBe 200
        }
      }

      "a view is in neither Save 4 Later nor backend" in {
        given()
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer[S4LFlatRateScheme].isEmpty
          .vatScheme.isBlank

        whenReady(controller.show(request))(res => res.header.status mustBe 200)
      }
    }
  }


  "posting an answer to the Join FRS question" must {
    "return an Redirect to the next page" when {
      "user answered Yes" in {
        given()
          .postRequest(Map("joinFrsRadio" -> "true")) //ordering matters! oops
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer[S4LFlatRateScheme].contains(JoinFrsView(selection = false))
          .s4lContainer[S4LFlatRateScheme].isUpdatedWith(JoinFrsView(selection = true))

        whenReady(controller.submit(request))(res => res.header.status mustBe 303)
      }

      "user answered No" in {
        given()
          .postRequest(Map("joinFrsRadio" -> "false")) //ordering matters! oops
          .user.isAuthorised
          .currentProfile.withProfile()
          .s4lContainer[S4LFlatRateScheme].contains(JoinFrsView(selection = true))
          .s4lContainer[S4LFlatRateScheme].isUpdatedWith(JoinFrsView(selection = false))
          .vatScheme.isBlank
          .vatScheme.isUpdatedWith(VatFlatRateScheme(joinFrs = true))

        whenReady(controller.submit(request))(res => res.header.status mustBe 303)
      }

    }
  }

}
