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
import models.view.frs.JoinFrsView
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import support.AppAndStubs

import scala.concurrent.ExecutionContext.Implicits.global

class JoinFrsControllerISpec extends PlaySpec with AppAndStubs with ScalaFutures {

  def controller: JoinFrsController = app.injector.instanceOf(classOf[JoinFrsController])

  "JoinFrsController" must {
    "return an OK status" when {
      "a view is in Save 4 Later" in {
        given()
          .user.isAuthorised
          .s4lContainer[S4LFlatRateScheme].contains(JoinFrsView(selection = true))

        whenReady(controller.show(request))(res => res.header.status === 200)
      }

      "a view is in neither Save 4 Later nor backend" in {
        given()
          .user.isAuthorised
          .s4lContainer[S4LFlatRateScheme].isEmpty
          .vatScheme.isBlank

        whenReady(controller.show(request))(res => res.header.status === 200)
      }

    }
  }

}
