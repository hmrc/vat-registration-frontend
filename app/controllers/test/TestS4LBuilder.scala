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

package controllers.test

import java.time.LocalDate
import javax.inject.Singleton

import features.officer.models.view._
import features.tradingDetails.TradingDetails
import models._
import models.api._
import models.external.{Name, Officer}
import features.sicAndCompliance.models._
import models.view.test.TestSetup
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import models.view.vatFinancials.{EstimateZeroRatedSales, ZeroRatedSales}


@Singleton
class TestS4LBuilder {

  def tradingDetailsFromData(data: TestSetup): TradingDetails = {
    data.tradingDetailsBlock match {
      case Some(tdb) => TradingDetails(tdb.tradingNameView, tdb.euGoods, tdb.applyEori)
      case None => TradingDetails()
    }
  }

  def vatFinancialsFromData(data: TestSetup): S4LVatFinancials = {
    val fin = data.vatFinancials

    val zeroRatedTurnover = fin.zeroRatedSalesChoice.map(ZeroRatedSales.apply)
    val zeroRatedTurnoverEstimate = fin.zeroRatedTurnoverEstimate.map(x => EstimateZeroRatedSales(x.toLong))

    S4LVatFinancials(
      zeroRatedTurnover = zeroRatedTurnover,
      zeroRatedTurnoverEstimate = zeroRatedTurnoverEstimate
    )
  }

  def vatSicAndComplianceFromData(data: TestSetup): SicAndCompliance = {
    val base = data.sicAndCompliance
    val compliance: SicAndCompliance = base.labourCompanyProvideWorkers.fold(SicAndCompliance())(_ =>
      SicAndCompliance(
          companyProvideWorkers = base.labourCompanyProvideWorkers.flatMap(x => Some(CompanyProvideWorkers(x))),
          workers = base.labourWorkers.flatMap(x => Some(Workers(x.toInt))),
          temporaryContracts = base.labourTemporaryContracts.flatMap(x => Some(TemporaryContracts(x))),
          skilledWorkers = base.labourSkilledWorkers.flatMap(x => Some(SkilledWorkers(x)))))



    compliance.copy(
      description = base.businessActivityDescription.map(BusinessActivityDescription(_)),
      mainBusinessActivity = Some(MainBusinessActivityView(SicCode(
        id = base.mainBusinessActivityId.getOrElse(""),
        description = base.mainBusinessActivityDescription.getOrElse(""),
        displayDetails = base.mainBusinessActivityDisplayDetails.getOrElse("")
      )))
    )
  }

  def vatContactFromData(data: TestSetup): S4LVatContact = {
    val businessContactDetails = data.vatContact.email.map(_ =>
      BusinessContactDetails(data.vatContact.email.get,
        data.vatContact.daytimePhone,
        data.vatContact.mobile,
        data.vatContact.website))

    val address: Option[ScrsAddress] = data.vatContact.line1.map(_ =>
      ScrsAddress(
        line1 = data.vatContact.line1.getOrElse(""),
        line2 = data.vatContact.line2.getOrElse(""),
        line3 = data.vatContact.line3,
        line4 = data.vatContact.line4,
        postcode = data.vatContact.postcode,
        country = data.vatContact.country))

    val ppob: Option[PpobView] = address.map(a =>
    PpobView(addressId = a.id, address = Some(a)))

    S4LVatContact(
      businessContactDetails = businessContactDetails,
      ppob = ppob)
  }

  def buildLodgingOfficerFromTestData(data: TestSetup): LodgingOfficer = {
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

    val dob: Option[LocalDate] = data.lodgingOfficer.dobDay.map(_ =>
      LocalDate.of(
        data.lodgingOfficer.dobYear.getOrElse("1900").toInt,
        data.lodgingOfficer.dobMonth.getOrElse("1").toInt,
        data.lodgingOfficer.dobDay.getOrElse("1").toInt))

    val nino = data.lodgingOfficer.nino

    val completionCapacity = data.lodgingOfficer.role.map(_ => {
      val officer = Officer(
        name = Name(data.lodgingOfficer.firstname,
          data.lodgingOfficer.othernames,
          data.lodgingOfficer.surname.getOrElse("")),
        role = data.lodgingOfficer.role.getOrElse(""))
      CompletionCapacityView(officer)
    })

    val contactDetails: Option[ContactDetailsView] = data.lodgingOfficer.email.map(_ =>
      ContactDetailsView(
        email = data.lodgingOfficer.email,
        mobile = data.lodgingOfficer.mobile,
        daytimePhone = data.lodgingOfficer.phone))

    val formerName: Option[FormerNameView] = data.lodgingOfficer.formernameChoice.collect {
      case "true" => FormerNameView(true, data.lodgingOfficer.formername)
      case "false" => FormerNameView(false, None)
    }

    val formerNameDate: Option[FormerNameDateView] = data.lodgingOfficer.formernameChangeDay.map(_ => {
      FormerNameDateView(LocalDate.of(
        data.lodgingOfficer.formernameChangeYear.getOrElse("1900").toInt,
        data.lodgingOfficer.formernameChangeMonth.getOrElse("1").toInt,
        data.lodgingOfficer.formernameChangeDay.getOrElse("1").toInt))
    })

    LodgingOfficer(
      previousAddress = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      homeAddress = homeAddress.map(a => HomeAddressView(a.id, Some(a))),
      securityQuestions = dob.map(SecurityQuestionsView(_, nino.getOrElse(""))),
      completionCapacity = completionCapacity,
      contactDetails = contactDetails,
      formerName = formerName,
      formerNameDate = formerNameDate
    )
  }

  def vatFrsFromData(data: TestSetup): S4LFlatRateScheme = {

    val joinFrs: Option[String] = data.vatFlatRateScheme.joinFrs
    val annualCostsInclusive: Option[String] = data.vatFlatRateScheme.annualCostsInclusive
    val annualCostsLimited: Option[String] = data.vatFlatRateScheme.annualCostsLimited
    val registerForFrs: Option[String] = data.vatFlatRateScheme.registerForFrs

    val frsStartDate = data.vatFlatRateScheme.frsStartDateChoice match {
      case None => FrsStartDateView()
      case Some("DIFFERENT_DATE") => FrsStartDateView(dateType = "DIFFERENT_DATE", date = Some(LocalDate.of(
        data.vatFlatRateScheme.frsStartDateYear.map(_.toInt).get,
        data.vatFlatRateScheme.frsStartDateMonth.map(_.toInt).get,
        data.vatFlatRateScheme.frsStartDateDay.map(_.toInt).get
      )))

      case Some(t) => FrsStartDateView(t, None)
    }

    S4LFlatRateScheme(
      joinFrs = joinFrs.map(a => JoinFrsView(a.toBoolean)),
      frsStartDate = Some(frsStartDate),
      annualCostsInclusive = annualCostsInclusive.map(AnnualCostsInclusiveView(_)),
      annualCostsLimited = annualCostsLimited.map(AnnualCostsLimitedView(_)),
      registerForFrs = registerForFrs.map(a => RegisterForFrsView(a.toBoolean))
    )

  }

}
