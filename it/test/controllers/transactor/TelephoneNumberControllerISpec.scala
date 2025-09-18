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

import forms.TransactorTelephoneForm.telephoneNumberKey
import itutil.ControllerISpec
import models.TransactorDetails
import org.jsoup.Jsoup
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class TelephoneNumberControllerISpec extends ControllerISpec {
  val url: String = controllers.transactor.routes.TelephoneNumberController.show.url

  private val testPhoneNumber = "12345 123456"
  val cleanedPhoneNumber = "12345123456"
  val testDetails: TransactorDetails = TransactorDetails(
    telephone = Some(testPhoneNumber)
  )

  s"GET $url" must {
    "show the view" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()
      whenReady(response) { res =>
        res.status mustBe OK
      }
    }

    "show the view with organisation name" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](Some(testDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).get()

      whenReady(response) { res =>
        res.status mustBe OK

        Jsoup.parse(res.body).getElementById(telephoneNumberKey).attr("value") mustBe testPhoneNumber
      }
    }
  }

  s"POST $url" must {
    "Redirect to Transactor Email Address page" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](TransactorDetails(telephone = Some(cleanedPhoneNumber)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(telephoneNumberKey -> testPhoneNumber))

      whenReady(res) { res =>
        res.status mustBe SEE_OTHER
        res.header("LOCATION") mustBe Some(controllers.transactor.routes.TransactorCaptureEmailAddressController.show.url)
      }
    }

    "return BAD_REQUEST if any of the validation fails for submitted telephone number" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post("")
      whenReady(res) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
