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

import controllers.applicant.{routes => applicantRoutes}
import itutil.ControllerISpec
import models._
import models.api._
import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class PreviousAddressControllerISpec extends ControllerISpec {

  val email = "test@t.test"
  val nino = "SR123456C"
  val role = "03"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)

  val pageUrl: String = routes.PreviousAddressController.show.url

  s"GET $pageUrl" must {
    "redirect user back to the Current Address route if address missing" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(currentAddress = None)), testRegId, None)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.HomeAddressController.redirectToAlf.url)
    }

    "render the page for a transactor" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(noPreviousAddress = None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))
        .registrationApi.getSection[TransactorDetails](Some(validTransactorDetails))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe false
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe false
    }

    "render the page with prepop" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(previousAddress = Some(address), noPreviousAddress = Some(false))))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res: WSResponse = await(buildClient(pageUrl).get())

      res.status mustBe OK
      Jsoup.parse(res.body).select("input[id=value]").hasAttr("checked") mustBe false
      Jsoup.parse(res.body).select("input[id=value-no]").hasAttr("checked") mustBe true
    }
  }

  s"POST $pageUrl" must {
    "redirect to International Address capture if the user is a NETP" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(NETP)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(noPreviousAddress = None, entity = Some(testNetpSoleTrader))))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(noPreviousAddress = Some(false), entity = Some(testNetpSoleTrader)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NETP,
        fixedEstablishmentInManOrUk = false
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(pageUrl)
        .post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.InternationalPreviousAddressController.show.url)
    }

    "redirect to International Address capture if the user is a Non UK Company" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(NonUkNonEstablished)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(noPreviousAddress = None, entity = Some(testMinorEntity))))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(noPreviousAddress = Some(false), entity = Some(testMinorEntity)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(
        partyType = NonUkNonEstablished,
        fixedEstablishmentInManOrUk = false
      )))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(pageUrl).post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.InternationalPreviousAddressController.show.url)
    }

    "redirect to previous address controller if the overseas user is established in UK" in new Setup {
      implicit val format = ApplicantDetails.apiFormat(NETP)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(noPreviousAddress = None, entity = Some(testNetpSoleTrader))))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(noPreviousAddress = Some(false), entity = Some(testNetpSoleTrader)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(partyType = NETP)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val res = await(buildClient(pageUrl).post(Map("value" -> Seq("false"))))

      res.status mustBe SEE_OTHER
      res.header(HeaderNames.LOCATION) mustBe Some(routes.PreviousAddressController.previousAddress.url)
    }

    "patch Applicant Details in backend when no previous address" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(previousAddress = None, noPreviousAddress = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(pageUrl).post(Map("value" -> Seq("true")))
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }

    "redirect to Alf with form errors for an invalid answer and missing current address in a transactor journey" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails.copy(currentAddress = None)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(pageUrl).post(Map("value" -> ""))

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(routes.HomeAddressController.redirectToAlf.url)
      }
    }

    "redirect to ALF callback for requesting previous address in a non-transactor journey" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(routes.PreviousAddressController.previousAddress.url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }

    "redirect to ALF callback for requesting previous address in a transactor journey" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(routes.PreviousAddressController.previousAddress.url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }

    "return page with form errors for an invalid answer" in new Setup {
      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .registrationApi.replaceSection[ApplicantDetails](validFullApplicantDetails.copy(previousAddress = None, noPreviousAddress = Some(true)))
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response = buildClient(pageUrl).post(Map("value" -> ""))

      whenReady(response) { res =>
        res.status mustBe BAD_REQUEST
      }
    }
  }

  "GET Txm ALF callback for Previous Address" must {
    "patch Applicant Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "GB"
      val addressPostcode = "BN3 1JU"

      val testApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(
        previousAddress = Some(Address(
          addressLine1,
          Some(addressLine2),
          postcode = Some(addressPostcode),
          country = Some(Country(Some(addressCountry), Some("United Kingdom"))),
          addressValidated = true
        )),
        noPreviousAddress = Some(false)
      )

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .registrationApi.replaceSection[ApplicantDetails](testApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(applicantRoutes.PreviousAddressController.addressLookupCallback(id = addressId).url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

}
