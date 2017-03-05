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
import enums.CacheKeys
import forms.vatDetails.test.TestSetupForm
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
      taxableTurnover <- s4LService.fetchAndGet[TaxableTurnover](CacheKeys.TaxableTurnover.toString)
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration](CacheKeys.VoluntaryRegistration.toString)
      startDate <- s4LService.fetchAndGet[StartDate](CacheKeys.StartDate.toString)
      tradingName <- s4LService.fetchAndGet[TradingName](CacheKeys.TradingName.toString)
      companyBankAccount <- s4LService.fetchAndGet[CompanyBankAccount](CacheKeys.CompanyBankAccount.toString)
      estimateVatTurnover <- s4LService.fetchAndGet[EstimateVatTurnover](CacheKeys.EstimateVatTurnover.toString)
      zeroRatedSales <- s4LService.fetchAndGet[ZeroRatedSales](CacheKeys.ZeroRatedSales.toString)
      estimateZeroRatedSales <- s4LService.fetchAndGet[EstimateZeroRatedSales](CacheKeys.EstimateZeroRatedSales.toString)
      vatChargeExpectancy <- s4LService.fetchAndGet[VatChargeExpectancy](CacheKeys.VatChargeExpectancy.toString)
      vatReturnFrequency <- s4LService.fetchAndGet[VatReturnFrequency](CacheKeys.VatReturnFrequency.toString)
      accountingPeriod <- s4LService.fetchAndGet[AccountingPeriod](CacheKeys.AccountingPeriod.toString)

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
        estimateVatTurnover.flatMap(_.vatTurnoverEstimate.map(_.toString)),
        zeroRatedSales.map(_.yesNo),
        estimateZeroRatedSales.flatMap(_.zeroRatedSalesEstimate.map(_.toString)),
        vatChargeExpectancy.map(_.yesNo),
        vatReturnFrequency.map(_.frequencyType),
        accountingPeriod.flatMap(_.accountingPeriod)
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test_setup(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    // TODO Special case
    def saveStartDate(data: TestSetup) = {
      s4LService.saveForm[StartDate](CacheKeys.StartDate.toString, data.startDateChoice
      match {
        case None => StartDate()
        case Some(a) => StartDate(a,
          data.startDateDay.map(_.toInt),
          data.startDateMonth.map(_.toInt),
          data.startDateYear.map(_.toInt))
      })
    }

    def saveToS4Later[T](data: Option[String], key: CacheKeys.Value, f: Option[String] => T, fmt: Format[T]): Future[Any] = {
      implicit val format = fmt
      data
      match {
        case None => Future.successful()
        case Some(_) => s4LService.saveForm(key.toString, f(data))
      }
    }

    def saveToS4LaterData[T](matchData: Option[String], data: TestSetup, key: CacheKeys.Value, f: TestSetup => T, fmt: Format[T]): Future[Any] = {
      implicit val format = fmt
      matchData
      match {
        case None => Future.successful()
        case Some(_) => s4LService.saveForm(key.toString, f(data))
      }
    }

    TestSetupForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test_setup(formWithErrors)))
      }, {
        data: TestSetup => {
          for {
            _ <- saveToS4Later(data.taxableTurnoverChoice, CacheKeys.TaxableTurnover, { x => TaxableTurnover(x.get) }, TaxableTurnover.format )
            _ <- saveToS4Later(data.voluntaryChoice, CacheKeys.VoluntaryRegistration, { x => VoluntaryRegistration(x.get) }, VoluntaryRegistration.format )
            _ <- saveStartDate(data)
            _ <- saveToS4LaterData(data.tradingNameChoice, data, CacheKeys.TradingName, { d => TradingName(d.tradingNameChoice.get, Some(data.tradingName.getOrElse("")))}, TradingName.format)
            _ <- saveToS4Later(data.companyBankAccountChoice, CacheKeys.CompanyBankAccount, { x => CompanyBankAccount(x.get) }, CompanyBankAccount.format )
            _ <- saveToS4Later(data.estimateVatTurnover, CacheKeys.EstimateVatTurnover, { x => EstimateVatTurnover(Some(x.get.toLong))}, EstimateVatTurnover.format)
            _ <- saveToS4Later(data.zeroRatedSalesChoice, CacheKeys.ZeroRatedSales, { x => ZeroRatedSales(x.get) }, ZeroRatedSales.format )
            _ <- saveToS4Later(data.zeroRatedSalesEstimate, CacheKeys.EstimateZeroRatedSales, { x => EstimateZeroRatedSales(Some(x.get.toLong))}, EstimateZeroRatedSales.format)
            _ <- saveToS4Later(data.vatChargeExpectancyChoice, CacheKeys.VatChargeExpectancy, { x => VatChargeExpectancy(x.get) }, VatChargeExpectancy.format )
            _ <- saveToS4Later(data.vatReturnFrequency, CacheKeys.VatReturnFrequency, { x => VatReturnFrequency(x.get) }, VatReturnFrequency.format )
            _ <- saveToS4Later(data.accountingPeriod, CacheKeys.AccountingPeriod, { x => AccountingPeriod(x) }, AccountingPeriod.format)

          } yield Ok("Test setup complete")
        }
      })
  })

}
