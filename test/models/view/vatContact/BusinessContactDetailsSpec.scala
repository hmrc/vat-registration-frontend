/*
 * Copyright 2017 HM Revenue & Customs
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

package models.view.vatContact

import fixtures.VatRegistrationFixture
import models.api.{VatContact, VatDigitalContact}
import models.{ApiModelTransformer, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class BusinessContactDetailsSpec extends UnitSpec with VatRegistrationFixture with Inside {

  "toApi" should {
    val initialVatContact = VatContact(VatDigitalContact(email = "initial@com", tel = None, mobile = None), website = None)

    val newBusinessContactDetails =
      BusinessContactDetails(email = "asd@xyz", daytimePhone = Some("123"), mobile = Some("123"), website = Some("qwe.com"))

    val updatedVatContact = VatContact(VatDigitalContact(email = "asd@xyz", tel = Some("123"), mobile = Some("123")), Some("qwe.com"))

    "update VatTradingDetails with new ApplyEori" in {
      ViewModelTransformer[BusinessContactDetails, VatContact]
        .toApi(newBusinessContactDetails, initialVatContact) shouldBe updatedVatContact
    }
  }

  "apply" should {

    "convert VatScheme without VatContact to empty view model" in {
      val vs = vatScheme(contact = None)
      ApiModelTransformer[BusinessContactDetails].toViewModel(vs) shouldBe None
    }

    "convert VatScheme with VatContact section to view model" in {
      val testVatContact = VatContact(VatDigitalContact(email = "test@com", tel = Some("123"), mobile = None), website = Some("test.com"))
      val vs = vatScheme(contact = Some(testVatContact))

      val expectedBusinessContactDetails = BusinessContactDetails(email = "test@com", daytimePhone = Some("123"), website = Some("test.com"))

      ApiModelTransformer[BusinessContactDetails].toViewModel(vs) shouldBe Some(expectedBusinessContactDetails)
    }

  }
}
