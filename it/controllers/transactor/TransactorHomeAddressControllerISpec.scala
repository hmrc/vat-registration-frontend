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
import play.api.http.HeaderNames
import play.api.test.Helpers._

import java.time.LocalDate

class TransactorHomeAddressControllerISpec extends ControllerISpec {

  val keyBlock = "transactor-details"
  val email = "test@test.com"
  val nino = "SR123456C"
  val role = "Director"
  val dob = LocalDate.of(1998, 7, 12)
  val addrLine1 = "8 Case Dodo"
  val addrLine2 = "seashore next to the pebble beach"
  val postcode = "TE1 1ST"
  val name = "Johnny Test"
  val telephone = "1234"

  val currentAddress = Address(line1 = testLine1, line2 = Some(testLine2), postcode = Some("TE 1ST"), addressValidated = true)


  "GET redirectToAlf" must {
    "redirect to ALF" in new Setup {
      given()
        .user.isAuthorised()
        .alfeJourney.initialisedSuccessfully()

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(routes.TransactorHomeAddressController.redirectToAlf.url).get()
      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some("continueUrl")
      }
    }
  }

  "GET Txm ALF callback for Home Address" must {
    "patch Transactor Details with ALF address in backend" in new Setup {
      val addressId = "addressId"
      val addressLine1 = "16 Coniston court"
      val addressLine2 = "Holland road"
      val addressCountry = "United Kingdom"
      val addressCountryCode = "GB"
      val addressPostcode = "BN3 1JU"

      given()
        .user.isAuthorised()
        .address(addressId, addressLine1, addressLine2, addressCountryCode, addressPostcode).isFound
        .registrationApi.getSection[TransactorDetails](None)
        .registrationApi.replaceSection[TransactorDetails](TransactorDetails(address = Some(Address(
        addressLine1,
        Some(addressLine2),
        None,
        None,
        None,
        Some(addressPostcode),
        Some(Country(Some(addressCountryCode), Some(addressCountry))),
        addressValidated = true
      ))))

      insertCurrentProfileIntoDb(currentProfile, sessionId)

      val response = buildClient(routes.TransactorHomeAddressController.addressLookupCallback(id = addressId).url).get()

      whenReady(response) { res =>
        res.status mustBe SEE_OTHER
        res.header(HeaderNames.LOCATION) mustBe Some(controllers.routes.TaskListController.show.url)
      }
    }
  }

}
