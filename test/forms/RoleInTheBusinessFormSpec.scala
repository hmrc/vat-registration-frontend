/*
 * Copyright 2023 HM Revenue & Customs
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

package forms

import forms.RoleInTheBusinessForm._
import models.api.{RegSociety, UkCompany}
import models.{Director, OtherDeclarationCapacity}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import services.ApplicantDetailsService.RoleInTheBusinessAnswer

class RoleInTheBusinessFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val otherRoleAnswer = "test"
  val otherRoleMissing = "validation.roleInTheBusiness.other.missing"
  val otherRoleTooLong = "validation.roleInTheBusiness.other.maxlen"
  val otherRoleInvalid = "validation.roleInTheBusiness.other.invalid"
  val otherRole3ptMissing = "validation.roleInTheBusiness.3pt.other.missing"
  val otherRole3ptTooLong = "validation.roleInTheBusiness.3pt.other.maxlen"
  val otherRole3ptInvalid = "validation.roleInTheBusiness.3pt.other.invalid"

  "RoleInTheBusinessForm" when {
    "the user is not a transactor" must {
      "fail without a selection" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = false).bind(Map[String, String]())

        form.errors mustBe Seq(FormError(roleInTheBusiness, roleInTheBusinessError))
      }
      "pass with a valid selection" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> director
        ))

        form.value mustBe Some(RoleInTheBusinessAnswer(Director, None))
      }
      "pass with a valid 'Other' selection and input" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> otherRoleAnswer
        ))

        form.value mustBe Some(RoleInTheBusinessAnswer(OtherDeclarationCapacity, Some(otherRoleAnswer)))
      }
      "fail when the 'Other' input is empty" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> ""
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRoleMissing))
      }
      "fail when the 'Other' input is too long" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> "a" * 101
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRoleTooLong))
      }
      "fail when the 'Other' input is invalid" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> "!"
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRoleInvalid))
      }
      "fail with a 'Other' selection when their partyType doesn't support it" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = false).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> otherRoleAnswer
        ))

        form.errors mustBe Seq(FormError(roleInTheBusiness, roleInTheBusinessError))
      }
    }
    "the user is a transactor" must {
      "fail without a selection" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = true).bind(Map[String, String]())

        form.errors mustBe Seq(FormError(roleInTheBusiness, roleInTheBusinessError3pt))
      }
      "pass with a valid selection" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> director
        ))

        form.value mustBe Some(RoleInTheBusinessAnswer(Director, None))
      }
      "pass with a valid 'Other' selection and input" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> otherRoleAnswer
        ))

        form.value mustBe Some(RoleInTheBusinessAnswer(OtherDeclarationCapacity, Some(otherRoleAnswer)))
      }
      "fail when the 'Other' input is empty" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> ""
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRole3ptMissing))
      }
      "fail when the 'Other' input is too long" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> "a" * 101
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRole3ptTooLong))
      }
      "fail when the 'Other' input is invalid" in {
        val form = RoleInTheBusinessForm(RegSociety, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> "!"
        ))

        form.errors mustBe Seq(FormError(otherRole, otherRole3ptInvalid))
      }
      "fail with a 'Other' selection when their partyType doesn't support it" in {
        val form = RoleInTheBusinessForm(UkCompany, isThirdParty = true).bind(Map[String, String](
          roleInTheBusiness -> other,
          otherRole -> otherRoleAnswer
        ))

        form.errors mustBe Seq(FormError(roleInTheBusiness, roleInTheBusinessError3pt))
      }
    }
  }
}
