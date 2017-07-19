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
import models.{ApiModelTransformer, S4LVatContact, ViewModelTransformer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec

class BusinessContactDetailsSpec extends UnitSpec with VatRegistrationFixture with Inside {

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

  "ViewModelFormat" should {
    val s4LVatContact: S4LVatContact = S4LVatContact(businessContactDetails = Some(validBusinessContactDetails))

    "extract businessContactDetails from vatContact" in {
      BusinessContactDetails.viewModelFormat.read(s4LVatContact) shouldBe Some(validBusinessContactDetails)
    }

    "update empty vatContact with businessContactDetails" in {
      BusinessContactDetails.viewModelFormat.update(validBusinessContactDetails, Option.empty[S4LVatContact]).businessContactDetails shouldBe Some(validBusinessContactDetails)
    }

    "update non-empty vatContact with businessContactDetails" in {
      BusinessContactDetails.viewModelFormat.update(validBusinessContactDetails, Some(s4LVatContact)).businessContactDetails shouldBe Some(validBusinessContactDetails)
    }

  }

}
