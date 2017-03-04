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

package models.view

import enums.CacheKeys
import models.api.{VatBankAccount, VatFinancials, VatScheme}
import models.{ApiModelTransformer, CacheKey, ViewModelTransformer}
import play.api.libs.json.Json

case class CompanyBankAccountDetails(accountName: String = "", accountNumber: String = "", sortCode: String = "")

object CompanyBankAccountDetails {

  implicit val format = Json.format[CompanyBankAccountDetails]

  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.financials.flatMap(_.bankAccount)
      .map(account => CompanyBankAccountDetails(account.accountName, account.accountNumber, account.accountSortCode))
      .getOrElse(CompanyBankAccountDetails())
  }

  implicit val viewModelTransformer = ViewModelTransformer { (c: CompanyBankAccountDetails, g: VatFinancials) =>
    g.copy(bankAccount = Some(VatBankAccount(c.accountName, c.accountNumber, c.sortCode)))
  }


  implicit val cacheKey = CacheKey[CompanyBankAccountDetails](CacheKeys.CompanyBankAccountDetails)
}
