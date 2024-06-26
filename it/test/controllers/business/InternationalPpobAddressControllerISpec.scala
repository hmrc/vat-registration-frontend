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

package controllers.business

import itutil.ControllerISpec
import models.Business
import models.api.{Address, Country}
import org.jsoup.Jsoup
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.test.Helpers._

class InternationalPpobAddressControllerISpec extends ControllerISpec {

  val url = "/principal-place-business/international"
  val testForeignCountry = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress = address.copy(country = Some(testForeignCountry))

  "GET /principal-place-business/international" when {
    "reading from backend" must {
      "return OK when the ApplicantDetails block is empty" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[Business](None, testRegId)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK
      }
      "return OK and pre-populate when the ApplicantDetails block contains an address" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[Business](Some(businessDetails.copy(ppobAddress = Some(testForeignAddress))), testRegId)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
    "when reading from the backend" must {
      "return OK and pre-populate the page" in new Setup {
        given
          .user.isAuthorised()
          .registrationApi.getSection[Business](
            Some(businessDetails.copy(ppobAddress = Some(testForeignAddress)))
          )(Business.apiKey, Business.format)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).get())

        res.status mustBe OK

        val doc = Jsoup.parse(res.body)
        doc.select("input[id=line1]").`val`() mustBe testLine1
        doc.select("input[id=line2]").`val`() mustBe testLine2
        doc.select("option[value=Norway]").hasAttr("selected") mustBe true
      }
    }
  }

  "POST /principal-place-business/international" must {
    "Store the address and redirect to the previous address page if a minimal address is provided" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None, testRegId)
        .registrationApi.replaceSection[Business](Business(ppobAddress = Some(testShortForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> "line1",
        "line2" -> "line2",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEmailController.show.url)
    }
    "Store the address and redirect to the previous address page if a full address is provided" in new Setup {
      val fullAddress = testForeignAddress.copy(line3 = Some("line3"), line4 = Some("line4"), line5 = Some("line5"), postcode = Some("AB12 3YZ"), addressValidated = false)
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None, testRegId)
        .registrationApi.replaceSection[Business](Business(ppobAddress = Some(fullAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> "line1",
        "line2" -> "line2",
        "line3" -> "line3",
        "line4" -> "line4",
        "line5" -> "line5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessEmailController.show.url)
    }
    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
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
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
      )))

      res.status mustBe BAD_REQUEST
    }
    "return BAD_REQUEST if country is UK" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[Business](None, testRegId)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "United Kingdom"
      )))

      res.status mustBe BAD_REQUEST
    }

    "return BAD_REQUEST if postcode missing and country requires postcode" in new Setup {

      def assertMissingPostcode(country: String): Assertion = {
        given
          .user.isAuthorised()
          .registrationApi.getRegistration(emptyVatSchemeNetp)
          .registrationApi.getSection[Business](None, testRegId)

        insertCurrentProfileIntoDb(currentProfile, sessionString)

        val res = await(buildClient(url).post(Map(
          "line1" -> "testLine1",
          "line2" -> "testLine2",
          "line3" -> "testLine3",
          "line4" -> "testLine4",
          "line5" -> "testLine5",
          "country" -> country
        )))

        res.status mustBe BAD_REQUEST
      }

      List("Isle of Man", "Guernsey", "Jersey").foreach(assertMissingPostcode)
    }
  }

}
