/*
 * Copyright 2018 HM Revenue & Customs
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

package models.view.vatLodgingOfficer

import features.officer.models.view.SecurityQuestionsView
import fixtures.VatRegistrationFixture
import models.api._
import models.{ApiModelTransformer, S4LVatLodgingOfficer}
import org.scalatest.Inside
import uk.gov.hmrc.play.test.UnitSpec
import models.S4LVatLodgingOfficer.{viewModelFormatSecuQuestions, modelTransformerSecuQuestions}

class SecurityQuestionsViewSpec extends UnitSpec with VatRegistrationFixture with Inside {
  val testDOB = DateOfBirth(1,12,1999)
  val emptyName = Name(None, None, "", None)
  val officerSecurityQuestions = SecurityQuestionsView(testDOB, testNino, Some(emptyName))
  val address = ScrsAddress(line1 = "current", line2 = "address", postcode = Some("postcode"))

  "apiModelTransformer" should {

    "convert VatScheme with VatLodgingOfficer details into a OfficerSecurityQuestionsView" in {
      val emptyOfficer = OfficerContactDetails(None, None, None)
      val vatLodgingOfficer = VatLodgingOfficer(
        Some(address),
        Some(testDOB),
        Some(testNino),
        Some(""),
        Some(emptyName),
        Some(changeOfName),
        Some(currentOrPreviousAddress),
        Some(emptyOfficer))
      val vs = vatScheme().copy(lodgingOfficer = Some(vatLodgingOfficer))

      modelTransformerSecuQuestions.toViewModel(vs) shouldBe Some(officerSecurityQuestions)
    }

    "convert VatScheme without VatLodgingOfficer to empty view model" in {
      val vs = vatScheme().copy(lodgingOfficer = None)
      modelTransformerSecuQuestions.toViewModel(vs) shouldBe None
    }

  }

  "ViewModelFormat" should {
    val s4LVatLodgingOfficer: S4LVatLodgingOfficer = S4LVatLodgingOfficer(officerSecurityQuestions = Some(officerSecurityQuestions))

    "extract OfficerSecurityQuestionsView from lodgingOfficer" in {
      viewModelFormatSecuQuestions.read(s4LVatLodgingOfficer) shouldBe Some(officerSecurityQuestions)
    }

    "update empty lodgingOfficer with OfficerSecurityQuestionsView" in {
      viewModelFormatSecuQuestions.update(officerSecurityQuestions, Option.empty[S4LVatLodgingOfficer]).
        officerSecurityQuestions shouldBe Some(officerSecurityQuestions)
    }

    "update non-empty lodgingOfficer with OfficerSecurityQuestionsView" in {
      viewModelFormatSecuQuestions.update(officerSecurityQuestions, Some(s4LVatLodgingOfficer)).
        officerSecurityQuestions shouldBe Some(officerSecurityQuestions)
    }
  }
}
