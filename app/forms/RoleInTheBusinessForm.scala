/*
 * Copyright 2026 HM Revenue & Customs
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

import forms.FormValidation.{maxLenText, nonEmptyValidText, stopOnFail}
import models._
import models.api._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import services.ApplicantDetailsService.RoleInTheBusinessAnswer
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import scala.util.matching.Regex


object RoleInTheBusinessForm {

  val roleInTheBusiness = "value"
  val otherRole = "otherRole"

  val director = "director"
  val companySecretary = "companySecretary"
  val trustee = "trustee"
  val boardMember = "boardMember"
  val other = "other"

  val roleInTheBusinessError = "validation.roleInTheBusiness.missing"
  val roleInTheBusinessError3pt = "validation.roleInTheBusiness.3pt.missing"
  val otherErrorKey = "roleInTheBusiness.other"
  val otherErrorKey3pt = "roleInTheBusiness.3pt.other"

  val regex: Regex = """^[0-9a-zA-Z &`\-\''\.^]+$""".r
  val maxLength = 100

  def apply(partyType: PartyType, isThirdParty: Boolean): Form[RoleInTheBusinessAnswer] = Form(
    mapping(
      roleInTheBusiness -> of(formatter(partyType, isThirdParty)),
      otherRole -> mandatoryIf(
        isEqual(roleInTheBusiness, other),
        text.verifying(stopOnFail(
          nonEmptyValidText(regex)(if (isThirdParty) otherErrorKey3pt else otherErrorKey),
          maxLenText(maxLength)(if (isThirdParty) otherErrorKey3pt else otherErrorKey)
        ))
      )
    )(RoleInTheBusinessAnswer.apply)(RoleInTheBusinessAnswer.unapply)
  )

  def formatter(partyType: PartyType, isThirdParty: Boolean): Formatter[RoleInTheBusiness] = new Formatter[RoleInTheBusiness] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], RoleInTheBusiness] = {
      data.get(key) match {
        case Some(`director`) => Right(Director)
        case Some(`companySecretary`) => Right(CompanySecretary)
        case Some(`trustee`) if partyType.equals(Trust) => Right(Trustee)
        case Some(`boardMember`) if Seq(RegSociety, UnincorpAssoc).contains(partyType) => Right(BoardMember)
        case Some(`other`) if Seq(RegSociety, UnincorpAssoc, NonUkNonEstablished).contains(partyType) => Right(OtherDeclarationCapacity)
        case _ => Left(Seq(FormError(key, if (isThirdParty) roleInTheBusinessError3pt else roleInTheBusinessError)))
      }
    }

    override def unbind(key: String, value: RoleInTheBusiness): Map[String, String] = {
      val stringValue = value match {
        case Director => director
        case CompanySecretary => companySecretary
        case Trustee => trustee
        case BoardMember => boardMember
        case OtherDeclarationCapacity => other
      }
      Map(key -> stringValue)
    }
  }
}