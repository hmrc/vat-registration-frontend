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
import models.view.sicAndCompliance.{BusinessActivityDescription, CulturalComplianceQ1}
import models.view.test._
import models.view.vatChoice.{StartDate, TaxableTurnover, VoluntaryRegistration}
import models.view.vatFinancials._
import models.view.vatTradingDetails.TradingName
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
      businessActivityDescription <- s4LService.fetchAndGet[BusinessActivityDescription]()
      sicStub <- s4LService.fetchAndGet[SicStub]()
      culturalComplianceQ1 <- s4LService.fetchAndGet[CulturalComplianceQ1]()

      testSetup = TestSetup(
        VatChoiceTestSetup(
          taxableTurnover.map(_.yesNo),
          voluntaryRegistration.map(_.yesNo),
          startDate.map(_.dateType),
          startDate.flatMap(_.day.map(_.toString)),
          startDate.flatMap(_.month.map(_.toString)),
          startDate.flatMap(_.year.map(_.toString))),
        VatTradingDetailsTestSetup(
          tradingName.map(_.yesNo),
          tradingName.flatMap(_.tradingName)),
        VatFinancialsTestSetup(
          companyBankAccount.map(_.yesNo),
          companyBankAccountDetails.map(_.accountName),
          companyBankAccountDetails.map(_.accountNumber),
          companyBankAccountDetails.map(_.sortCode),
          estimateVatTurnover.map(_.vatTurnoverEstimate.toString),
          zeroRatedSales.map(_.yesNo),
          estimateZeroRatedSales.map(_.zeroRatedSalesEstimate.toString),
          vatChargeExpectancy.map(_.yesNo),
          vatReturnFrequency.map(_.frequencyType),
          accountingPeriod.map(_.accountingPeriod)),
        SicAndComplianceTestSetup(
          businessActivityDescription.map(_.description),
          sicStub.map(_.sicCode1.getOrElse("")),
          sicStub.map(_.sicCode2.getOrElse("")),
          sicStub.map(_.sicCode3.getOrElse("")),
          sicStub.map(_.sicCode4.getOrElse("")),
          culturalComplianceQ1.map(_.yesNo))
      )
      form = TestSetupForm.form.fill(testSetup)
    } yield Ok(views.html.pages.test_setup(form))
  })

  def submit: Action[AnyContent] = authorised.async(implicit user => implicit request => {
    // TODO Special case
    def saveStartDate(data: TestSetup) = {
      s4LService.saveForm[StartDate](data.vatChoice.startDateChoice
      match {
        case None => StartDate()
        case Some(a) => StartDate(a,
          data.vatChoice.startDateDay.map(_.toInt),
          data.vatChoice.startDateMonth.map(_.toInt),
          data.vatChoice.startDateYear.map(_.toInt))
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
            _ <- saveToS4Later(data.vatChoice.taxableTurnoverChoice, data, { x => TaxableTurnover(x.vatChoice.taxableTurnoverChoice.get) })
            _ <- saveToS4Later(data.vatChoice.voluntaryChoice, data, { x => VoluntaryRegistration(x.vatChoice.voluntaryChoice.get) })
            _ <- saveToS4Later(data.vatTradingDetails.tradingNameChoice, data, { x => TradingName(x.vatTradingDetails.tradingNameChoice.get, Some(data.vatTradingDetails.tradingName.getOrElse(""))) })
            _ <- saveToS4Later(data.vatFinancials.companyBankAccountChoice, data, { x => CompanyBankAccount(x.vatFinancials.companyBankAccountChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.companyBankAccountName, data, {
              x => CompanyBankAccountDetails(x.vatFinancials.companyBankAccountName.get,
              x.vatFinancials.companyBankAccountNumber.get, x.vatFinancials.sortCode.get)
            })
            _ <- saveToS4Later(data.vatFinancials.estimateVatTurnover, data, { x => EstimateVatTurnover(x.vatFinancials.estimateVatTurnover.get.toLong) })
            _ <- saveToS4Later(data.vatFinancials.zeroRatedSalesChoice, data, { x => ZeroRatedSales(x.vatFinancials.zeroRatedSalesChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.zeroRatedSalesEstimate, data, { x => EstimateZeroRatedSales(x.vatFinancials.zeroRatedSalesEstimate.get.toLong) })
            _ <- saveToS4Later(data.vatFinancials.vatChargeExpectancyChoice, data, { x => VatChargeExpectancy(x.vatFinancials.vatChargeExpectancyChoice.get) })
            _ <- saveToS4Later(data.vatFinancials.vatReturnFrequency, data, { x => VatReturnFrequency(x.vatFinancials.vatReturnFrequency.get) })
            _ <- saveToS4Later(data.vatFinancials.accountingPeriod, data, { x => AccountingPeriod(x.vatFinancials.accountingPeriod.get) })
            _ <- saveToS4Later(data.sicAndCompliance.businessActivityDescription, data, { x => BusinessActivityDescription(x.sicAndCompliance.businessActivityDescription.get) })
            _ <- saveToS4Later(data.sicAndCompliance.sicCode1, data, { x => SicStub(Some(x.sicAndCompliance.sicCode1.getOrElse("")),
                                                                                    Some(x.sicAndCompliance.sicCode2.getOrElse("")),
                                                                                    Some(x.sicAndCompliance.sicCode3.getOrElse("")),
                                                                                    Some(x.sicAndCompliance.sicCode4.getOrElse(""))) })
            _ <- saveToS4Later(data.sicAndCompliance.culturalComplianceQ1, data, { x => CulturalComplianceQ1(x.sicAndCompliance.culturalComplianceQ1.get) })
          } yield Ok("Test setup complete")
        }
      })
  })

}