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

package forms

import forms.FormValidation.{maxLenText, nonEmptyValidText}
import models._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.play.mappers.StopOnFirstFail
import uk.gov.voa.play.form.ConditionalMappings.{isEqual, mandatoryIf}

import scala.util.matching.Regex


object DeclarationCapacityForm {

  val declarationCapacity = "declarationCapacity"
  val otherRole = "otherRole"

  val accountant = "accountant"
  val representative = "representative"
  val boardMember = "boardMember"
  val authorisedEmployee = "authorisedEmployee"
  val other = "other"

  val declarationCapacityMissing = "validation.declarationCapacity.missing"
  val otherErrorKey = "declarationCapacity.other"

  val regex: Regex = """^[0-9a-zA-Z &`\-\''\.^]+$""".r
  val maxLength = 100

  def apply(): Form[DeclarationCapacityAnswer] = Form(
    mapping(
      declarationCapacity -> of(formatter),
      otherRole -> mandatoryIf(
        isEqual(declarationCapacity, other),
        text.verifying(StopOnFirstFail(
          nonEmptyValidText(regex)(otherErrorKey),
          maxLenText(maxLength)(otherErrorKey)
        ))
      )
    )(DeclarationCapacityAnswer.apply)(DeclarationCapacityAnswer.unapply)
  )

  def formatter: Formatter[DeclarationCapacity] = new Formatter[DeclarationCapacity] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], DeclarationCapacity] = {
      data.get(key) match {
        case Some(`accountant`) => Right(AccountantAgent)
        case Some(`representative`) => Right(Representative)
        case Some(`boardMember`) => Right(BoardMember)
        case Some(`authorisedEmployee`) => Right(AuthorisedEmployee)
        case Some(`other`) => Right(Other)
        case _ => Left(Seq(FormError(key, declarationCapacityMissing)))
      }
    }

    override def unbind(key: String, value: DeclarationCapacity): Map[String, String] = {
      val stringValue = value match {
        case AccountantAgent => accountant
        case Representative => representative
        case BoardMember => boardMember
        case AuthorisedEmployee => authorisedEmployee
        case Other => other
      }
      Map(key -> stringValue)
    }
  }
}