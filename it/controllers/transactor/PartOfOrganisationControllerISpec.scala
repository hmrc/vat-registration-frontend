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

import controllers.transactor.{routes => transactorRoutes}
import forms.PartOfOrganisationForm._
import itutil.ControllerISpec
import models._
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class PartOfOrganisationControllerISpec extends ControllerISpec {

  val url: String = transactorRoutes.PartOfOrganisationController.show.url

  val testDetails: TransactorDetails = TransactorDetails(isPartOfOrganisation = Some(true))

  s"GET $url" must {
    List(None, Some(testDetails)).foreach { transactorDetails =>
      s"show the view with transactor details ${transactorDetails.fold("not set")(_ => "set")}" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](transactorDetails)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).get()

        whenReady(response) { res =>
          res.status mustBe OK
        }
      }
    }

    s"POST $url" must {
      "redirect to Organisation Name page when yes is selected" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](None)
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(isPartOfOrganisation = Some(true)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("true")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.OrganisationNameController.show.url)
        }
      }

      "clear organisationName and redirect to Declaration Capacity page when no is selected" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](Some(TransactorDetails(isPartOfOrganisation = Some(true), organisationName = Some("test"))))
          .registrationApi.replaceSection[TransactorDetails](TransactorDetails(isPartOfOrganisation = Some(false)))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post(Map(yesNo -> Seq("false")))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(transactorRoutes.DeclarationCapacityController.show.url)
        }
      }

      "return BAD_REQUEST if no option selected" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post("")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}