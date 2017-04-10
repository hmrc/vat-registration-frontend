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

package forms.vatFinancials.vatBankAccount

import cats.Show
import forms.validation.FormValidation.BankAccount._
import play.api.data.Form
import play.api.data.Forms._

case class CompanyBankAccountDetailsForm(accountName: String, accountNumber: String, sortCode: SortCode)

object CompanyBankAccountDetailsForm {

  private val ACCOUNT_TYPE = "companyBankAccount"

  val form = Form(
    mapping(
      "accountName" -> text.verifying(accountName(ACCOUNT_TYPE)),
      "accountNumber" -> text.verifying(accountNumber(ACCOUNT_TYPE)),
      "sortCode" -> mapping(
        "part1" -> text,
        "part2" -> text,
        "part3" -> text
      )(SortCode.apply)(SortCode.unapply).verifying(accountSortCode(ACCOUNT_TYPE))
    )(CompanyBankAccountDetailsForm.apply)(CompanyBankAccountDetailsForm.unapply)
  )

}

case class SortCode(part1: String, part2: String, part3: String)

object SortCode {

  val Pattern = """^([0-9]{2})-([0-9]{2})-([0-9]{2})$""".r
  val PartPattern = """^[0-9]{2}$""".r

  def parse(sortCode: String): Option[SortCode] = sortCode match {
    case Pattern(p1, p2, p3) => Some(SortCode(p1, p2, p3))
    case _ => None
  }

  implicit val show: Show[SortCode] = Show.show(sc => {
    val str = Seq(sc.part1.trim, sc.part2.trim, sc.part3.trim).mkString("-")
    //to avoid producing a "--" for sort codes where all double-digits are blank (not entered)
    if (str == "--") "" else str
  })
}

