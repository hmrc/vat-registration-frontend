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

import forms.OrganisationNameForm.organisationNameKey
import itutil.ControllerISpec
import models.TransactorDetails
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.Future

class OrganisationNameControllerISpec extends ControllerISpec {
  val url: String = controllers.transactor.routes.OrganisationNameController.show.url

  val orgName = "testOrgName"
  val testDetails: TransactorDetails = TransactorDetails(
    organisationName = Some(orgName)
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

        Jsoup.parse(res.body).getElementById(organisationNameKey).attr("value") mustBe orgName
      }
    }
  }

  s"POST $url" must {
    "Redirect to Declaration Capacity" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](testDetails)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: Future[WSResponse] = buildClient(url).post(Map(organisationNameKey -> orgName))

      whenReady(res) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.DeclarationCapacityController.show.url)
      }
    }

    "return BAD_REQUEST for missing trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post("")
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST for invalid trading name" in new Setup {
      given().user.isAuthorised()
      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(url).post(Map(organisationNameKey -> "a" * 161))
      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }
}
