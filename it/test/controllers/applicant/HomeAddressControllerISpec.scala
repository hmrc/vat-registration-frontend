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
import models.ApplicantDetails
import models.api.{Address, Country, EligibilitySubmissionData, UkCompany}
import play.api.http.HeaderNames
import play.api.libs.json.Format
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._

import java.time.LocalDate
import scala.concurrent.Future

class HomeAddressControllerISpec extends ControllerISpec {

  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"
  val dob: LocalDate = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"

  val currentAddress: Address = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)


  "GET redirectToAlf" must {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(applicantRoutes.HomeAddressController.redirectToAlf.url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }

    "redirect to ALF for transactor journey" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData.copy(isTransactor = true)))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(applicantRoutes.HomeAddressController.redirectToAlf.url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
  }

  "GET Txm ALF callback for Home Address" must {
    "patch Applicant Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "GB"
      val addressPostcode = "BN3 1JU"

      val testApplicantDetails: ApplicantDetails = validFullApplicantDetails.copy(currentAddress = Some(Address(
        addressLine1,
        Some(addressLine2),
        postcode = Some(addressPostcode),
        country = Some(Country(Some(addressCountry), Some("United Kingdom"))),
        addressValidated = true
      )))

      implicit val format: Format[ApplicantDetails] = ApplicantDetails.apiFormat(UkCompany)
      given()
        .user.isAuthorised()
        .registrationApi.getSection[ApplicantDetails](Some(validFullApplicantDetails))
        .address(addressId, addressLine1, addressLine2, addressCountry, addressPostcode).isFound
        .registrationApi.replaceSection[ApplicantDetails](testApplicantDetails)
        .registrationApi.getSection[EligibilitySubmissionData](Some(testEligibilitySubmissionData))

      insertCurrentProfileIntoDb(currentProfile, sessionString)

      val response: Future[WSResponse] = buildClient(applicantRoutes.HomeAddressController.addressLookupCallback(id = addressId).url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(applicantRoutes.PreviousAddressController.show.url)
      }
    }
  }

}
