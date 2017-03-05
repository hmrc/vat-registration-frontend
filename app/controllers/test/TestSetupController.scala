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

    def saveTaxableTurnover(data: TestSetup): Future[Any] = {
      data.taxableTurnoverChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.TaxableTurnover.toString, TaxableTurnover(a))
      }
    }


    def saveVoluntaryRegistration(data: TestSetup): Future[Any] = {
      data.voluntaryChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.VoluntaryRegistration.toString, VoluntaryRegistration(a))
      }
    }

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

    def saveTradingName(data: TestSetup): Future[Any] = {
      data.tradingNameChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.TradingName.toString,
          TradingName(a, Some(data.tradingName.getOrElse(""))))
      }
    }

    def saveCompanyBankAccount(data: TestSetup): Future[Any] = {
      data.companyBankAccountChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.CompanyBankAccount.toString, CompanyBankAccount(a))
      }
    }

    def saveEstimateVatTurnover(data: TestSetup): Future[Any] = {
      data.estimateVatTurnover
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.EstimateVatTurnover.toString, EstimateVatTurnover(Some(a.toLong)))
      }
    }

    def saveZeroRatedSales(data: TestSetup): Future[Any] = {
      data.zeroRatedSalesChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.ZeroRatedSales.toString, ZeroRatedSales(a))
      }
    }

    def saveEstimateZeroRatedSales(data: TestSetup): Future[Any] = {
      data.zeroRatedSalesEstimate
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.EstimateZeroRatedSales.toString, EstimateZeroRatedSales(Some(a.toLong)))
      }
    }

    def saveVatChargeExpectancy(data: TestSetup): Future[Any] = {
      data.vatChargeExpectancyChoice
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.VatChargeExpectancy.toString, VatChargeExpectancy(a))
      }
    }

    def saveVatReturnFrequency(data: TestSetup): Future[Any] = {
      data.vatReturnFrequency
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.VatReturnFrequency.toString, VatReturnFrequency(a))
      }
    }

    def saveAccountingPeriod(data: TestSetup): Future[Any] = {
      data.accountingPeriod
      match {
        case None => Future.successful()
        case Some(a) => s4LService.saveForm(CacheKeys.AccountingPeriod.toString, AccountingPeriod(Some(a)))
      }
    }

    TestSetupForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test_setup(formWithErrors)))
      }, {
        data: TestSetup => {
          for {
            _ <- saveTaxableTurnover(data)
            _ <- saveVoluntaryRegistration(data)
            _ <- saveStartDate(data)
            _ <- saveTradingName(data)
            _ <- saveCompanyBankAccount(data)
            _ <- saveEstimateVatTurnover(data)
            _ <- saveZeroRatedSales(data)
            _ <- saveEstimateZeroRatedSales(data)
            _ <- saveVatChargeExpectancy(data)
            _ <- saveVatReturnFrequency(data)
            _ <- saveAccountingPeriod(data)

          } yield Ok("Test setup complete")
        }
      })
  })

}
