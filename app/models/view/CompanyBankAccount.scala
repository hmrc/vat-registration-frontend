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
import models.api.VatScheme
import models.{ApiModelTransformer, CacheKey}
import play.api.libs.json.Json

case class CompanyBankAccount(yesNo: String)

object CompanyBankAccount {

  val COMPANY_BANK_ACCOUNT_YES = "COMPANY_BANK_ACCOUNT_YES"
  val COMPANY_BANK_ACCOUNT_NO = "COMPANY_BANK_ACCOUNT_NO"

  val valid = (item: String) => List(COMPANY_BANK_ACCOUNT_YES, COMPANY_BANK_ACCOUNT_NO).contains(item.toUpperCase)

  implicit val format = Json.format[CompanyBankAccount]

  implicit val modelTransformer = ApiModelTransformer { vs: VatScheme =>
    vs.financials.map {
      _.bankAccount match {
        case Some(_) => CompanyBankAccount(COMPANY_BANK_ACCOUNT_YES)
        case None => CompanyBankAccount(COMPANY_BANK_ACCOUNT_NO)
      }
    }
  }

  implicit val cacheKey = CacheKey[CompanyBankAccount](CacheKeys.CompanyBankAccount)
}
