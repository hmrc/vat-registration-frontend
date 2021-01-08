/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{BeingSetup, NameChange, NoUKBankAccount, OverseasAccount}
import play.api.data.{Form, FormError}
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter

object NoUKBankAccountForm {

  val noUKBankAccount: String = "value"

  val beingSetup: String = "beingSetup"
  val overseasAccount: String = "overseasAccount"
  val nameChange: String = "nameChange"

  val noUKBankAccountError: String = "pages.noUKBankAccount.error"

  def apply(): Form[NoUKBankAccount] = Form(
    single(
      noUKBankAccount -> of(formatter)
    )
  )

  def formatter: Formatter[NoUKBankAccount] = new Formatter[NoUKBankAccount] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], NoUKBankAccount] = {
      data.get(key) match {
        case Some(`beingSetup`) => Right(BeingSetup)
        case Some(`overseasAccount`) => Right(OverseasAccount)
        case Some(`nameChange`) => Right(NameChange)
        case _ => Left(Seq(FormError(key, noUKBankAccountError)))
      }
    }

    override def unbind(key: String, value: NoUKBankAccount): Map[String, String] = {
      val stringValue = value match {
        case BeingSetup => beingSetup
        case OverseasAccount => overseasAccount
        case NameChange => nameChange
      }
      Map(key -> stringValue)
    }
  }

  def form: Form[NoUKBankAccount] = Form(
    single(
      noUKBankAccount -> of(formatter)
    )
  )
}
