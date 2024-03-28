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

package forms

import models._
import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object NoUKBankAccountForm {

  val noUKBankAccount: String = "value"

  val beingSetup: String = "beingSetup"
  val overseasAccount: String = "overseasAccount"
  val nameChange: String = "nameChange"
  val accountNotInBusinessName: String = "accountNotInBusinessName"
  val dontWantToProvide: String = "dontWantToProvide"

  val noUKBankAccountError: String = "pages.noUKBankAccount.error"

  private def formatter: Formatter[NoUKBankAccount] = new Formatter[NoUKBankAccount] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], NoUKBankAccount] = {
      data.get(key) match {
        case Some(`beingSetup`) => Right(BeingSetupOrNameChange)
        case Some(`overseasAccount`) => Right(OverseasAccount)
        case Some(`accountNotInBusinessName`) => Right(AccountNotInBusinessName)
        case Some(`dontWantToProvide`) => Right(DontWantToProvide)
        case _ => Left(Seq(FormError(key, noUKBankAccountError)))
      }
    }

    override def unbind(key: String, value: NoUKBankAccount): Map[String, String] = {
      val stringValue = value match {
        case BeingSetupOrNameChange => beingSetup
        case OverseasAccount => overseasAccount
        case AccountNotInBusinessName => accountNotInBusinessName
        case DontWantToProvide => dontWantToProvide
        case NameChange => beingSetup //Converting old answer to new
        case _ => ""
      }
      Map(key -> stringValue)
    }
  }

  def form: Form[NoUKBankAccount] = Form(
    single(
      noUKBankAccount -> of(
          formatter
      )
    )
  )
}
