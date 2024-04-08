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

package controllers.applicant

import itutil.ControllerISpec
import models.ApplicantDetails
import models.api.{Address, Country, EligibilitySubmissionData, UkCompany}
import org.jsoup.Jsoup
import org.scalatest.Assertion
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.test.Helpers._

class InternationalHomeAddressControllerISpec extends ControllerISpec {

  val url = "/home-address/international"
  val testForeignCountry = Country(Some("NO"), Some("Norway"))
  val testShortForeignAddress = Address(testLine1, Some(testLine2), country = Some(testForeignCountry))
  val testForeignAddress = Address("testLine1", Some("testLine2"), Some("testLine3"), Some("testLine4"), Some("testLine5"), Some("AB12 3YZ"), country = Some(testForeignCountry))

  "GET /home-address/international" when {
    "return OK when the ApplicantDetails block is empty" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[ApplicantDetails](None)

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).get())

      res.status mustBe OK
    }

    "return OK and pre-populate the page" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      val appDetails = ApplicantDetails(
        personalDetails = Some(testPersonalDetails),
        currentAddress = Some(testShortForeignAddress)
      )
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[ApplicantDetails](Some(appDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).get())

      res.status mustBe OK

      val doc = Jsoup.parse(res.body)
      doc.select("input[id=line1]").`val`() mustBe testLine1
      doc.select("input[id=line2]").`val`() mustBe testLine2
      doc.select("option[value=Norway]").hasAttr("selected") mustBe true
    }

    "return OK and pre-populate the page for transactor journey" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      val appDetails = ApplicantDetails(
        personalDetails = Some(testPersonalDetails),
        currentAddress = Some(testShortForeignAddress)
      )
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(appDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).get())

      res.status mustBe OK

      val doc = Jsoup.parse(res.body)
      doc.select("input[id=line1]").`val`() mustBe testLine1
      doc.select("input[id=line2]").`val`() mustBe testLine2
      doc.select("option[value=Norway]").hasAttr("selected") mustBe true
    }
  }

  "POST /home-address/international" must {
    "Store the address and redirect to the previous address page if a minimal address is provided" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](ApplicantDetails(currentAddress = Some(testShortForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> testLine1,
        "line2" -> testLine2,
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.show.url)
    }

    "Store the address for transactor journey and redirect to the previous address page if a minimal address is provided" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(currentAddress = Some(testShortForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> testLine1,
        "line2" -> testLine2,
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.show.url)
    }

    "Store the address and redirect to the previous address page if a full address is provided" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))
        .registrationApi.getSection[ApplicantDetails](None)
        .registrationApi.replaceSection[ApplicantDetails](ApplicantDetails(currentAddress = Some(testForeignAddress)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
        "line1" -> "testLine1",
        "line2" -> "testLine2",
        "line3" -> "testLine3",
        "line4" -> "testLine4",
        "line5" -> "testLine5",
        "postcode" -> "AB12 3YZ",
        "country" -> "Norway"
      )))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.show.url)
    }

    "return BAD_REQUEST if line 1 is missing" in new Setup {
      given
        .user.isAuthorised()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

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
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(url).post(Map(
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
      def assertMissingPostcode(country: String): Assertion = {
        given
          .user.isAuthorised()
          .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

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

      List("United Kingdom", "Isle of Man", "Guernsey", "Jersey").foreach(assertMissingPostcode)
    }
  }

}
