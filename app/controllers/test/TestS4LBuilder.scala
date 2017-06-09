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

import java.time.LocalDate

import models.api._
import models.view.sicAndCompliance.BusinessActivityDescription
import models.view.sicAndCompliance.cultural.NotForProfit
import models.view.sicAndCompliance.financial._
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.test.TestSetup
import models.view.vatContact.BusinessContactDetails
import models.view.vatLodgingOfficer._
import models.view.vatTradingDetails.TradingNameView
import models.view.vatTradingDetails.vatChoice.{StartDateView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import models.view.vatTradingDetails.vatEuTrading.{ApplyEori, EuGoods}
import models.{S4LTradingDetails, S4LVatContact, S4LVatLodgingOfficer, S4LVatSicAndCompliance}

class TestS4LBuilder {

  def tradingDetailsFromData(data: TestSetup): S4LTradingDetails = {
    val taxableTurnover: Option[String] = data.vatChoice.taxableTurnoverChoice

    val startDate = data.vatChoice.startDateChoice match {
      case None => StartDateView()
      case Some("SPECIFIC_DATE") => StartDateView(dateType = "SPECIFIC_DATE", date = Some(LocalDate.of(
        data.vatChoice.startDateYear.map(_.toInt).get,
        data.vatChoice.startDateMonth.map(_.toInt).get,
        data.vatChoice.startDateDay.map(_.toInt).get
      )))
      case Some("BUSINESS_START_DATE") => StartDateView(dateType = "BUSINESS_START_DATE", ctActiveDate = Some(LocalDate.of(
        data.vatChoice.startDateYear.map(_.toInt).get,
        data.vatChoice.startDateMonth.map(_.toInt).get,
        data.vatChoice.startDateDay.map(_.toInt).get
      )))
      case Some(t) => StartDateView(t, None)
    }

    val voluntaryRegistration: Option[String] = data.vatChoice.voluntaryChoice
    val voluntaryRegistrationReason: Option[String] = data.vatChoice.voluntaryRegistrationReason

    val tradingName = data.vatTradingDetails.tradingNameChoice.map(_ =>
      TradingName(
        selection = data.vatTradingDetails.tradingNameChoice.map(_.toBoolean).getOrElse(false),
        tradingName = data.vatTradingDetails.tradingName))

    val euGoods: Option[String] = data.vatTradingDetails.euGoods
    val applyEori: Option[String] = data.vatTradingDetails.applyEori

    S4LTradingDetails(
      taxableTurnover = taxableTurnover.map(TaxableTurnover(_)),
      startDate = Some(startDate),
      voluntaryRegistration = voluntaryRegistration.map(VoluntaryRegistration(_)),
      voluntaryRegistrationReason = voluntaryRegistrationReason.map(VoluntaryRegistrationReason(_)),
      tradingName = tradingName.map(t => TradingNameView(t.selection.toString, t.tradingName)),
      euGoods = euGoods.map(EuGoods(_)),
      applyEori = applyEori.map(a => ApplyEori(a.toBoolean))
    )
  }

  def vatSicAndComplianceFromData(data: TestSetup): S4LVatSicAndCompliance = {
    val base = data.sicAndCompliance
    val compliance: S4LVatSicAndCompliance =
      (base.culturalNotForProfit, base.labourCompanyProvideWorkers, base.financialAdviceOrConsultancy) match {
        case (Some(_), None, None) => S4LVatSicAndCompliance(
          notForProfit = Some(NotForProfit(base.culturalNotForProfit.get)))
        case(None, Some(_), None) => S4LVatSicAndCompliance(
          companyProvideWorkers = base.labourCompanyProvideWorkers.flatMap(x => Some(CompanyProvideWorkers(x))),
          workers = base.labourWorkers.flatMap(x => Some(Workers(x.toInt))),
          temporaryContracts = base.labourTemporaryContracts.flatMap(x => Some(TemporaryContracts(x))),
          skilledWorkers = base.labourSkilledWorkers.flatMap(x => Some(SkilledWorkers(x))))
        case(None, None, Some(_)) => S4LVatSicAndCompliance(
          adviceOrConsultancy = base.financialAdviceOrConsultancy.flatMap(x => Some(AdviceOrConsultancy(x.toBoolean))),
          actAsIntermediary = base.financialActAsIntermediary.flatMap(x => Some(ActAsIntermediary(x.toBoolean))),
          chargeFees = base.financialChargeFees.flatMap(x => Some(ChargeFees(x.toBoolean))),
          leaseVehicles = base.financialLeaseVehiclesOrEquipment.flatMap(x => Some(LeaseVehicles(x.toBoolean))),
          additionalNonSecuritiesWork = base.financialAdditionalNonSecuritiesWork.flatMap(x => Some(AdditionalNonSecuritiesWork(x.toBoolean))),
          discretionaryInvestmentManagementServices =
            base.financialDiscretionaryInvestment.flatMap(x => Some(DiscretionaryInvestmentManagementServices(x.toBoolean))),
          investmentFundManagement = base.financialInvestmentFundManagement.flatMap(x => Some(InvestmentFundManagement(x.toBoolean))),
          manageAdditionalFunds = base.financialManageAdditionalFunds.flatMap(x => Some(ManageAdditionalFunds(x.toBoolean))))
        case (_, _, _) => S4LVatSicAndCompliance()
      }

    compliance.copy(description = base.businessActivityDescription.map(BusinessActivityDescription(_)))
  }

  def vatContactFromData(data: TestSetup): S4LVatContact = {
    val businessContactDetails = data.vatContact.email.map(_ =>
      BusinessContactDetails(data.vatContact.email.get,
        data.vatContact.daytimePhone,
        data.vatContact.mobile,
        data.vatContact.website))

    S4LVatContact(businessContactDetails = businessContactDetails)
  }

  def vatLodgingOfficerFromData(data: TestSetup): S4LVatLodgingOfficer = {
    val homeAddress: Option[ScrsAddress] = data.officerHomeAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerHomeAddress.line1.getOrElse(""),
        line2 = data.officerHomeAddress.line2.getOrElse(""),
        line3 = data.officerHomeAddress.line3,
        line4 = data.officerHomeAddress.line4,
        postcode = data.officerHomeAddress.postcode,
        country = data.officerHomeAddress.country))

    val threeYears: Option[String] = data.officerPreviousAddress.threeYears

    val previousAddress: Option[ScrsAddress] = data.officerPreviousAddress.line1.map(_ =>
      ScrsAddress(
        line1 = data.officerPreviousAddress.line1.getOrElse(""),
        line2 = data.officerPreviousAddress.line2.getOrElse(""),
        line3 = data.officerPreviousAddress.line3,
        line4 = data.officerPreviousAddress.line4,
        postcode = data.officerPreviousAddress.postcode,
        country = data.officerPreviousAddress.country))

    val dob: Option[LocalDate] = data.vatLodgingOfficer.dobDay.map(_ =>
      LocalDate.of(
        data.vatLodgingOfficer.dobYear.getOrElse("1900").toInt,
        data.vatLodgingOfficer.dobMonth.getOrElse("1").toInt,
        data.vatLodgingOfficer.dobDay.getOrElse("1").toInt))

    val nino = data.vatLodgingOfficer.nino

    val completionCapacity = data.vatLodgingOfficer.role.map(_ => {
      CompletionCapacity(
        name = Name(data.vatLodgingOfficer.firstname,
          data.vatLodgingOfficer.othernames,
          data.vatLodgingOfficer.surname.getOrElse("")),
        role = data.vatLodgingOfficer.role.getOrElse(""))
    })

    val contactDetails = data.vatLodgingOfficer.email.map(_ =>
      OfficerContactDetails(
        email = data.vatLodgingOfficer.email,
        mobile = data.vatLodgingOfficer.mobile,
        tel = data.vatLodgingOfficer.phone))

    val formerName = data.vatLodgingOfficer.formernameChoice.map(_ =>
      FormerName(
        selection = data.vatLodgingOfficer.formernameChoice.map(_.toBoolean).getOrElse(false),
        formerName = data.vatLodgingOfficer.formername))

    S4LVatLodgingOfficer(
      previousAddress = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      officerHomeAddress = homeAddress.map(a => OfficerHomeAddressView(a.id, Some(a))),
      officerDateOfBirth = dob.map(OfficerDateOfBirthView(_, completionCapacity.map(_.name))),
      officerNino = nino.map(OfficerNinoView(_)),
      completionCapacity = completionCapacity.map(CompletionCapacityView(_)),
      officerContactDetails = contactDetails.map(OfficerContactDetailsView(_)),
      formerName = formerName.map(FormerNameView(_))
    )
  }

}
