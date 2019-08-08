/*
 * Copyright 2019 HM Revenue & Customs
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

import features.businessContact.models.{BusinessContact, CompanyContactDetails}
import features.officer.models.view._
import features.sicAndCompliance.models._
import features.tradingDetails.TradingDetails
import models.api._
import models.view.test.TestSetup

object TestS4LBuilder {

  def tradingDetailsFromData(data: TestSetup): TradingDetails = {
    data.tradingDetailsBlock match {
      case Some(tdb) => TradingDetails(tdb.tradingNameView, tdb.euGoods)
      case None      => TradingDetails()
    }
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
      mainBusinessActivity = base.mainBusinessActivityId map { id =>
        MainBusinessActivityView(SicCode(
          code = id,
          description = base.mainBusinessActivityDescription.getOrElse(""),
          displayDetails = base.mainBusinessActivityDisplayDetails.getOrElse("")
        ))
      }
    )
  }

  def vatContactFromData(data: TestSetup): BusinessContact = {
    val address: Option[ScrsAddress] = data.vatContact.line1.map(_ =>
      ScrsAddress(
        line1 = data.vatContact.line1.getOrElse(""),
        line2 = data.vatContact.line2.getOrElse(""),
        line3 = data.vatContact.line3,
        line4 = data.vatContact.line4,
        postcode = data.vatContact.postcode,
        country = data.vatContact.country
      )
    )

    data.vatContact.email.map(_ => BusinessContact(
      companyContactDetails = Some(CompanyContactDetails(
        data.vatContact.email.get,
        data.vatContact.daytimePhone,
        data.vatContact.mobile,
        data.vatContact.website
      )),
      ppobAddress = address
    )).getOrElse(BusinessContact())
  }

  def buildLodgingOfficerFromTestData(data: TestSetup): LodgingOfficer = {
    val homeAddress: Option[ScrsAddress] = data.officerHomeAddress.line1.map(_ => ScrsAddress(
      line1    = data.officerHomeAddress.line1.getOrElse(""),
      line2    = data.officerHomeAddress.line2.getOrElse(""),
      line3    = data.officerHomeAddress.line3,
      line4    = data.officerHomeAddress.line4,
      postcode = data.officerHomeAddress.postcode,
      country  = data.officerHomeAddress.country)
    )

    val threeYears: Option[String] = data.officerPreviousAddress.threeYears

    val previousAddress: Option[ScrsAddress] = data.officerPreviousAddress.line1.map(_ => ScrsAddress(
      line1    = data.officerPreviousAddress.line1.getOrElse(""),
      line2    = data.officerPreviousAddress.line2.getOrElse(""),
      line3    = data.officerPreviousAddress.line3,
      line4    = data.officerPreviousAddress.line4,
      postcode = data.officerPreviousAddress.postcode,
      country  = data.officerPreviousAddress.country)
    )

    val dob: Option[LocalDate] = data.lodgingOfficer.dobDay.map(_ =>
      LocalDate.of(
        data.lodgingOfficer.dobYear.getOrElse("1900").toInt,
        data.lodgingOfficer.dobMonth.getOrElse("1").toInt,
        data.lodgingOfficer.dobDay.getOrElse("1").toInt))

    val contactDetails: Option[ContactDetailsView] = data.lodgingOfficer.email.map(_ => ContactDetailsView(
      email         = data.lodgingOfficer.email,
      mobile        = data.lodgingOfficer.mobile,
      daytimePhone  = data.lodgingOfficer.phone)
    )

    val formerName: Option[FormerNameView] = data.lodgingOfficer.formernameChoice.collect {
      case "true"  => FormerNameView(true, data.lodgingOfficer.formername)
      case "false" => FormerNameView(false, None)
    }

    val formerNameDate: Option[FormerNameDateView] = data.lodgingOfficer.formernameChangeDay.map(_ => {
      FormerNameDateView(LocalDate.of(
        data.lodgingOfficer.formernameChangeYear.getOrElse("1900").toInt,
        data.lodgingOfficer.formernameChangeMonth.getOrElse("1").toInt,
        data.lodgingOfficer.formernameChangeDay.getOrElse("1").toInt))
    })

    LodgingOfficer(
      previousAddress     = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      homeAddress         = homeAddress.map(a => HomeAddressView(a.id, Some(a))),
      securityQuestions   = dob.map(SecurityQuestionsView(_)),
      contactDetails      = contactDetails,
      formerName          = formerName,
      formerNameDate      = formerNameDate
    )
  }
}
