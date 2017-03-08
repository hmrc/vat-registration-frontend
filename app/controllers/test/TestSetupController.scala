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

package controllers.test

import javax.inject.Inject

import connectors.{KeystoreConnector, VatRegistrationConnector}
import controllers.{CommonPlayDependencies, VatRegistrationController}
import forms.vatDetails.test.TestSetupForm
import models.CacheKey
import models.view.{EstimateVatTurnover, StartDate, TradingName, _}
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService}

import scala.concurrent.Future

class TestSetupController @Inject()(s4LService: S4LService, vatRegistrationConnector: VatRegistrationConnector,
                                    ds: CommonPlayDependencies) extends VatRegistrationController(ds) with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  def show: Action[AnyContent] = authorised.async(body = implicit user => implicit request => {
    for {
      taxableTurnover <- s4LService.fetchAndGet[TaxableTurnover]()
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration]()
      startDate <- s4LService.fetchAndGet[StartDate]()
      tradingName <- s4LService.fetchAndGet[TradingName]()
      companyBankAccount <- s4LService.fetchAndGet[CompanyBankAccount]()
      companyBankAccountDetails <- s4LService.fetchAndGet[CompanyBankAccountDetails]()
      estimateVatTurnover <- s4LService.fetchAndGet[EstimateVatTurnover]()
      zeroRatedSales <- s4LService.fetchAndGet[ZeroRatedSales]()
      estimateZeroRatedSales <- s4LService.fetchAndGet[EstimateZeroRatedSales]()
      vatChargeExpectancy <- s4LService.fetchAndGet[VatChargeExpectancy]()
      vatReturnFrequency <- s4LService.fetchAndGet[VatReturnFrequency]()
      accountingPeriod <- s4LService.fetchAndGet[AccountingPeriod]()

      testSetup = TestSetup(
        taxableTurnover.map(_.yesNo),
        voluntaryRegistration.map(_.yesNo),
        startDate.map(_.dateType),
        startDate.flatMap(_.day.map(_.toString)),
        startDate.flatMap(_.month.map(_.toString)),
        startDate.flatMap(_.year.map(_.toString)),
        tradingName.map(_.yesNo),
        tradingName.flatMap(_.tradingName),
        companyBankAccount.map(_.yesNo),
        companyBankAccountDetails.map(_.accountName),
        companyBankAccountDetails.map(_.accountNumber),
        companyBankAccountDetails.map(_.sortCode),
        estimateVatTurnover.map(_.vatTurnoverEstimate.toString),
        zeroRatedSales.map(_.yesNo),
        estimateZeroRatedSales.map(_.zeroRatedSalesEstimate.toString),
        vatChargeExpectancy.map(_.yesNo),
        vatReturnFrequency.map(_.frequencyType),
        accountingPeriod.map(_.accountingPeriod)
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test_setup(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    // TODO Special case
    def saveStartDate(data: TestSetup) = {
      s4LService.saveForm[StartDate](data.startDateChoice
      match {
        case None => StartDate()
        case Some(a) => StartDate(a,
          data.startDateDay.map(_.toInt),
          data.startDateMonth.map(_.toInt),
          data.startDateYear.map(_.toInt))
      })
    }

    def saveToS4Later[T: Format : CacheKey](userEntered: Option[String], data: TestSetup, f: TestSetup => T): Future[Unit] =
      userEntered.map(_ => s4LService.saveForm(f(data)).map(_ => ())).getOrElse(Future.successful(()))

    TestSetupForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test_setup(formWithErrors)))
      }, {
        data: TestSetup => {
          for {
            _ <- saveStartDate(data)
            _ <- saveToS4Later(data.taxableTurnoverChoice, data, { x => TaxableTurnover(x.taxableTurnoverChoice.get) })
            _ <- saveToS4Later(data.voluntaryChoice, data, { x => VoluntaryRegistration(x.voluntaryChoice.get) })
            _ <- saveToS4Later(data.tradingNameChoice, data, { x => TradingName(x.tradingNameChoice.get, Some(data.tradingName.getOrElse(""))) })
            _ <- saveToS4Later(data.companyBankAccountChoice, data, { x => CompanyBankAccount(x.companyBankAccountChoice.get) })
            _ <- saveToS4Later(data.companyBankAccountName, data, { x => CompanyBankAccountDetails(x.companyBankAccountName.get, x.companyBankAccountNumber.get, x.sortCode.get) })
            _ <- saveToS4Later(data.estimateVatTurnover, data, { x => EstimateVatTurnover(x.estimateVatTurnover.get.toLong) })
            _ <- saveToS4Later(data.zeroRatedSalesChoice, data, { x => ZeroRatedSales(x.zeroRatedSalesChoice.get) })
            _ <- saveToS4Later(data.zeroRatedSalesEstimate, data, { x => EstimateZeroRatedSales(x.zeroRatedSalesEstimate.get.toLong) })
            _ <- saveToS4Later(data.vatChargeExpectancyChoice, data, { x => VatChargeExpectancy(x.vatChargeExpectancyChoice.get) })
            _ <- saveToS4Later(data.vatReturnFrequency, data, { x => VatReturnFrequency(x.vatReturnFrequency.get) })
            _ <- saveToS4Later(data.accountingPeriod, data, { x => AccountingPeriod(x.accountingPeriod.get) })
          } yield Ok("Test setup complete")
        }
      })
  })

}