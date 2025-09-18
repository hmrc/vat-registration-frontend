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

import itutil.ControllerISpec
import models.TransactorDetails
import models.api.{Address, Country}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

class TransactorInternationalAddressControllerISpec extends ControllerISpec {

  val url = "/your-address/international"
  val testForeignCountry: Country = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress: Address = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress: Address = Address("testLine1", Some("testLine2"), Some("testLine3"), Some("testLine4"), Some("testLine5"), Some("AB12 3YZ"), country = Some(testForeignCountry))

  "GET /your-address/international" when {
    "when reading from the backend" must {
      "return OK when the TransactorDetails block is empty" in new Setup {
        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](None)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK
      }

      "return OK and pre-populate the page" in new Setup {
        val trDetails: TransactorDetails = TransactorDetails(address = Some(testShortForeignAddress))

        given()
          .user.isAuthorised()
          .registrationApi.getSection[TransactorDetails](Some(trDetails))

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res: WSResponse = await(buildClient(url).get())

        res.status mustBe OK

        val doc: Document = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
  }

  "POST /your-address/international" must {
    "Store the address and redirect to the next page if a minimal address is provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](TransactorDetails(address = Some(testShortForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map(
        "line1" -> testLine1,
        "line2" -> testLine2,
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }
    "Store the address and redirect to the next page if a full address is provided" in new Setup {
      given()
        .user.isAuthorised()
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](TransactorDetails(address = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
    }
    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map(
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is missing" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ"
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is UK and postcode is missing" in new Setup {
      given()
        .user.isAuthorised()

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "country" -> "United Kingdom"
      )))

      res.status mustBe BAD_REQUEST
    }
  }

}
