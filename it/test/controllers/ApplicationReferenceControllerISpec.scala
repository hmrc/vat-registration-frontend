/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.RegistrationApiConnector.applicationReferenceKey
import featuretoggle.FeatureToggleSupport
import itFixtures.ITRegistrationFixtures
import itutil.ControllerISpec
import models.ApiKey
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import scala.concurrent.Future

class ApplicationReferenceControllerISpec extends ControllerISpec
  with ITRegistrationFixtures
  with FeatureToggleSupport {

  val testAppRef = "testAppRef"
  val url = "/register-for-vat/application-reference"


  "GET /application-reference" when {
    "a reference already exists in the users' registration" must {
      "return OK with a pre-filled form" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given()
          .user.isAuthorised()
          .registrationApi.getSection(Some(testAppRef))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        whenReady(buildClient(url).get()) { res =>
          res.status mustBe OK
          Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe testAppRef
        }
      }
    }
    "a reference doesn't exist in the users'registration" must {
      "return OK with a pre-filled form" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given()
          .user.isAuthorised()
          .registrationApi.getSection(None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        whenReady(buildClient(url).get()) { res =>
          res.status mustBe OK
          Jsoup.parse(res.body).select("[id=value]").first().`val`() mustBe ""
        }
      }
    }
  }

  "POST /application-reference" when {
    "submitted with valid reference value" must {
      "successfully redirect to honesty_declaration page" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given()
          .user.isAuthorised()
          .registrationApi.replaceSection(testAppRef)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post(Map("value" -> testAppRef))

        whenReady(response) { res =>
          res.status mustBe SEE_OTHER
          res.header(HeaderNames.LOCATION) mustBe Some(routes.HonestyDeclarationController.show.url)
        }
        verifyAudit()
      }
    }

    "submitted without auth.credentials" must {
      "redirect to INTERNAL_SERVER_ERROR" in new Setup {
        implicit val key: ApiKey[String] = applicationReferenceKey
        given()
          .user.isAuthorisedWithoutCredentials()
          .registrationApi.replaceSection(testAppRef)

        val response: Future[WSResponse] = buildClient(url).post(Map("value" -> testAppRef))

        whenReady(response) { res =>
          res.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "submitted with valid a missing reference value" must {
      "return a BAD_REQUEST" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post("")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }

    "submitted with an invalid reference number, too long" must {
      "return a BAD_REQUEST" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post("w" * 101)

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }

    "submitted with an invalid reference number, invalid characters" must {
      "return a BAD_REQUEST" in new Setup {
        given().user.isAuthorised()
        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val response: Future[WSResponse] = buildClient(url).post("«test»")

        whenReady(response) { res =>
          res.status mustBe BAD_REQUEST
        }
      }
    }
  }
}
