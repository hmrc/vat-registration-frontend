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
import models.view.{EstimateVatTurnover, StartDate, TradingName, _}
import play.api.mvc.{Action, AnyContent}
import services.{CommonService, S4LService}

import scala.concurrent.Future

class TestSetupController @Inject()(s4LService: S4LService, vatRegistrationConnector: VatRegistrationConnector,
                                    ds: CommonPlayDependencies) extends VatRegistrationController(ds) with CommonService {

  override val keystoreConnector: KeystoreConnector = KeystoreConnector

  def show: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    for {
      taxableTurnover <- s4LService.fetchAndGet[TaxableTurnover]()
      voluntaryRegistration <- s4LService.fetchAndGet[VoluntaryRegistration]()
      startDate <- s4LService.fetchAndGet[StartDate]()
      tradingName <- s4LService.fetchAndGet[TradingName]()
      companyBankAccount <- s4LService.fetchAndGet[CompanyBankAccount]()
      estimateVatTurnover <- s4LService.fetchAndGet[EstimateVatTurnover]()
      zeroRatedSales <- s4LService.fetchAndGet[ZeroRatedSales]()
      estimateZeroRatedSales <- s4LService.fetchAndGet[EstimateZeroRatedSales]()
      vatChargeExpectancy <- s4LService.fetchAndGet[VatChargeExpectancy]()
      vatReturnFrequency <- s4LService.fetchAndGet[VatReturnFrequency]()
      accountingPeriod <- s4LService.fetchAndGet[AccountingPeriod]()

      testSetup = TestSetup(if (taxableTurnover.isDefined) taxableTurnover.get.yesNo else "",
        if (voluntaryRegistration.isDefined) voluntaryRegistration.get.yesNo else "",
        if (startDate.isDefined) startDate.get.dateType else "",
        if (startDate.isDefined) s"${startDate.get.day.getOrElse("").toString}" else "",
        if (startDate.isDefined) s"${startDate.get.month.getOrElse("").toString}" else "",
        if (startDate.isDefined) s"${startDate.get.year.getOrElse("").toString}" else "",
        if (tradingName.isDefined) tradingName.get.yesNo else "",
        if (tradingName.isDefined) tradingName.get.tradingName.getOrElse("") else "",
        if (companyBankAccount.isDefined) companyBankAccount.get.yesNo else "",
        if (estimateVatTurnover.isDefined) estimateVatTurnover.get.vatTurnoverEstimate.getOrElse("").toString else "",
        if (zeroRatedSales.isDefined) zeroRatedSales.get.yesNo else "",
        if (estimateZeroRatedSales.isDefined) estimateZeroRatedSales.get.zeroRatedSalesEstimate.getOrElse("").toString else "",
        if (vatChargeExpectancy.isDefined) vatChargeExpectancy.get.yesNo else "",
        if (vatReturnFrequency.isDefined) vatReturnFrequency.get.frequencyType else "",
        if (accountingPeriod.isDefined) accountingPeriod.get.accountingPeriod.getOrElse("") else ""
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test_setup(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    TestSetupForm.form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.pages.test_setup(formWithErrors)))
      }, {
        data: TestSetup => {
          for {
            regId <- fetchRegistrationId
            _ <- s4LService.saveForm[TaxableTurnover](data.taxableTurnoverChoice
            match {
              case "" => TaxableTurnover()
              case _ => TaxableTurnover(data.taxableTurnoverChoice)
            })

            _ <- s4LService.saveForm[VoluntaryRegistration](data.voluntaryChoice
            match {
              case "" => VoluntaryRegistration()
              case _ => VoluntaryRegistration(data.voluntaryChoice)
            })

            _ <- s4LService.saveForm[StartDate](data.startDateChoice
            match {
              case "" => StartDate()
              case _ => StartDate(data.startDateChoice,
                Some(data.startDateDay.toInt),
                Some(data.startDateMonth.toInt),
                Some(data.startDateYear.toInt))
            })

            _ <- s4LService.saveForm[TradingName](data.tradingNameChoice
            match {
              case "" => TradingName()
              case _ => TradingName(data.tradingNameChoice, Some(data.tradingName))
            })

            _ <- s4LService.saveForm[CompanyBankAccount](data.companyBankAccountChoice
            match {
              case "" => CompanyBankAccount()
              case _ => CompanyBankAccount(data.companyBankAccountChoice)
            })

            _ <- s4LService.saveForm[EstimateVatTurnover](data.estimateVatTurnover
            match {
              case "" => EstimateVatTurnover()
              case _ => EstimateVatTurnover(Some(data.estimateVatTurnover.toLong))
            })

            _ <- s4LService.saveForm[ZeroRatedSales](data.zeroRatedSalesChoice
            match {
              case "" => ZeroRatedSales()
              case _ => ZeroRatedSales(data.zeroRatedSalesChoice)
            })

            _ <- s4LService.saveForm[EstimateZeroRatedSales](data.zeroRatedSalesEstimate
            match {
              case "" => EstimateZeroRatedSales()
              case _ => EstimateZeroRatedSales(Some(data.zeroRatedSalesEstimate.toLong))
            })

            _ <- s4LService.saveForm[VatChargeExpectancy](data.vatChargeExpectancyChoice
            match {
              case "" => VatChargeExpectancy()
              case _ => VatChargeExpectancy(data.vatChargeExpectancyChoice)
            })

            _ <- s4LService.saveForm[VatReturnFrequency](data.vatReturnFrequency
            match {
              case "" => VatReturnFrequency()
              case _ => VatReturnFrequency(data.vatReturnFrequency)
            })

            _ <- s4LService.saveForm[AccountingPeriod](data.accountingPeriod
            match {
              case "" => AccountingPeriod()
              case _ => AccountingPeriod(Some(data.accountingPeriod))
            })

          } yield Ok("Test setup complete")
        }
      })
  })


}
