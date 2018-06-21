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

package forms.test

import features.bankAccountDetails.models.{BankAccount, BankAccountDetails}
import features.returns.models.Frequency.Frequency
import features.returns.models.Stagger.Stagger
import features.returns.models.{Returns, Start}
import features.tradingDetails.{TradingDetails, TradingNameView}
import frs.FlatRateScheme
import models.TurnoverEstimates
import models.view.test._
import play.api.data.Forms._
import play.api.data.{Form, Mapping}

object TestSetupEligibilityForm {
  val form = Form(single(
    "eligibilityJson" -> text
  ))
}

object TestSetupForm {

  val sicAndComplianceTestSetupMapping = mapping(
    "businessActivityDescription" -> optional(text),
    "sicCode1" -> optional(text),
    "sicCode2" -> optional(text),
    "sicCode3" -> optional(text),
    "sicCode4" -> optional(text),
    "labourCompanyProvideWorkers" -> optional(text),
    "labourWorkers" -> optional(text),
    "labourTemporaryContracts" -> optional(text),
    "labourSkilledWorkers" -> optional(text),
    "mainBusinessActivityId" -> optional(text),
    "mainBusinessActivityDescription" -> optional(text),
    "mainBusinessActivityDisplayDetails" -> optional(text)
  )(SicAndComplianceTestSetup.apply)(SicAndComplianceTestSetup.unapply)

  val vatContactTestSetupMapping = mapping(
    "email" -> optional(text),
    "daytimePhone" -> optional(text),
    "mobile" -> optional(text),
    "website" -> optional(text),
    "line1" -> optional(text),
    "line2" -> optional(text),
    "line3" -> optional(text),
    "line4" -> optional(text),
    "postcode" -> optional(text),
    "country" -> optional(text)
  )(VatContactTestSetup.apply)(VatContactTestSetup.unapply)

  val vatLodgingOfficerTestSetup = mapping(
    "dobDay" -> optional(text),
    "dobMonth" -> optional(text),
    "dobYear" -> optional(text),
    "email" -> optional(text),
    "mobile" -> optional(text),
    "phone" -> optional(text),
    "formernameChoice" -> optional(text),
    "formername" -> optional(text),
    "formernameChangeDay" -> optional(text),
    "formernameChangeMonth" -> optional(text),
    "formernameChangeYear" -> optional(text)
  )(LodgingOfficerTestSetup.apply)(LodgingOfficerTestSetup.unapply)

  val officeHomeAddressMapping = mapping(
    "line1" -> optional(text),
    "line2" -> optional(text),
    "line3" -> optional(text),
    "line4" -> optional(text),
    "postcode" -> optional(text),
    "country" -> optional(text)
  )(OfficerHomeAddressTestSetup.apply)(OfficerHomeAddressTestSetup.unapply)

  val officePreviousAddressMapping = mapping(
    "threeYears" -> optional(text),
    "line1" -> optional(text),
    "line2" -> optional(text),
    "line3" -> optional(text),
    "line4" -> optional(text),
    "postcode" -> optional(text),
    "country" -> optional(text)
  )(OfficerPreviousAddressTestSetup.apply)(OfficerPreviousAddressTestSetup.unapply)

  val flatRateSchemeMapping = mapping(
    "joinFrs" -> optional(boolean),
    "overBusinessGoods" -> optional(boolean),
    "estimateTotalSales" -> optional(longNumber),
    "overBusinessGoodsPercent" -> optional(boolean),
    "useThisRate" -> optional(boolean),
    "start" -> optional(mapping(
      "date" -> optional(localDate)
    )(Start.apply)(Start.unapply)),
    "categoryOfBusiness" -> optional(text),
    "percent" -> optional(bigDecimal)
  )(FlatRateScheme.apply)(FlatRateScheme.unapply)

  val bankAccountMapping: Mapping[BankAccount] = mapping(
    "isProvided" -> boolean,
    "details"    -> optional(mapping(
      "accountName" -> text,
      "accountSortCode" -> text,
      "accountNumber" -> text
    )(BankAccountDetails.apply)(BankAccountDetails.unapply))
  )(BankAccount.apply)(BankAccount.unapply)

  val returnsMapping: Mapping[Returns] = mapping(
    "reclaimVatOnMostReturns" -> optional(boolean),
    "frequency"    -> optional(of[Frequency]),
    "staggerStart" -> optional(of[Stagger]),
    "start" -> optional(mapping(
      "date" -> optional(localDate)
    )(Start.apply)(Start.unapply))
  )(Returns.apply)(Returns.unapply)

  val tradingDetailsMapping = mapping(
    "tradingName" -> optional(mapping(
      "yesNo" -> boolean,
      "tradingName" -> optional(text)
    )(TradingNameView.apply)(TradingNameView.unapply)),
    "euGoods" -> optional(boolean)
  )(TradingDetails.apply)(TradingDetails.unapply)

  val form = Form(mapping(
    "vatContact" -> vatContactTestSetupMapping,
    "sicAndCompliance" -> sicAndComplianceTestSetupMapping,
    "officerHomeAddress" -> officeHomeAddressMapping,
    "officerPreviousAddress" -> officePreviousAddressMapping,
    "vatLodgingOfficer" -> vatLodgingOfficerTestSetup,
    "flatRateScheme" -> optional(flatRateSchemeMapping),
    "bankAccount" -> optional(bankAccountMapping),
    "returns" -> optional(returnsMapping),
    "tradingDetails" -> optional(tradingDetailsMapping)
  )(TestSetup.apply)(TestSetup.unapply))

}
