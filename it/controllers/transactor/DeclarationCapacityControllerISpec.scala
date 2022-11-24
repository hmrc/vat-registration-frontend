/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.transactor

import forms.DeclarationCapacityForm.{declarationCapacity, other, otherRole}
import itutil.ControllerISpec
import models.{DeclarationCapacityAnswer, OtherDeclarationCapacity, TransactorDetails}
import org.jsoup.Jsoup
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class DeclarationCapacityControllerISpec extends ControllerISpec {

  val url: String = controllers.transactor.routes.DeclarationCapacityController.show.url

  val testOtherRole = "testOtherRole"
  val testDetails = TransactorDetails(
    declarationCapacity = Some(DeclarationCapacityAnswer(OtherDeclarationCapacity, Some("testOtherRole")))
  )

  s"GET $url" must {
    "show the view" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").size() mustBe 0
      }
    }

    "show the view with prepopulated data" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(testDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementsByAttribute("checked").first().parent().text() mustBe "Other"
        Jsoup.parse(res.body).getElementById(otherRole).attr("value") mustBe testOtherRole
      }
    }
  }

  s"POST $url" must {
    "update backend and redirect to Transactor Identification" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](testDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(Map(
        declarationCapacity -> other,
        otherRole -> "testOtherRole"
      )))

      res.status mustBe SEE_OTHER
      res.header("LOCATION") mustBe Some(controllers.grs.routes.TransactorIdController.startJourney.url)
    }

    "return BAD_REQUEST if role selected as other and not set" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val res = await(buildClient(url).post(""))

      res.status mustBe BAD_REQUEST
    }
  }
}