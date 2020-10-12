/*
 * Copyright 2020 HM Revenue & Customs
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
import models.view._
import models.view.test.TestSetup
import models._
import models.external.{EmailAddress, EmailVerified}

object TestS4LBuilder {

  def tradingDetailsFromData(data: TestSetup): TradingDetails = {
    data.tradingDetailsBlock match {
      case Some(tdb) => TradingDetails(tdb.tradingNameView, tdb.euGoods)
      case None => TradingDetails()
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

  def buildApplicantDetailsFromTestData(data: TestSetup): ApplicantDetails = {
    val homeAddress: Option[ScrsAddress] = data.applicantHomeAddress.line1.map(_ => ScrsAddress(
      line1 = data.applicantHomeAddress.line1.getOrElse(""),
      line2 = data.applicantHomeAddress.line2.getOrElse(""),
      line3 = data.applicantHomeAddress.line3,
      line4 = data.applicantHomeAddress.line4,
      postcode = data.applicantHomeAddress.postcode,
      country = data.applicantHomeAddress.country)
    )

    val threeYears: Option[String] = data.applicantPreviousAddress.threeYears

    val previousAddress: Option[ScrsAddress] = data.applicantPreviousAddress.line1.map(_ => ScrsAddress(
      line1 = data.applicantPreviousAddress.line1.getOrElse(""),
      line2 = data.applicantPreviousAddress.line2.getOrElse(""),
      line3 = data.applicantPreviousAddress.line3,
      line4 = data.applicantPreviousAddress.line4,
      postcode = data.applicantPreviousAddress.postcode,
      country = data.applicantPreviousAddress.country)
    )

    val dob: Option[LocalDate] = data.applicantDetails.dobDay.map(_ =>
      LocalDate.of(
        data.applicantDetails.dobYear.getOrElse("1900").toInt,
        data.applicantDetails.dobMonth.getOrElse("1").toInt,
        data.applicantDetails.dobDay.getOrElse("1").toInt))

    val formerName: Option[FormerNameView] = data.applicantDetails.formernameChoice.collect {
      case "true" => FormerNameView(true, data.applicantDetails.formername)
      case "false" => FormerNameView(false, None)
    }

    val formerNameDate: Option[FormerNameDateView] = data.applicantDetails.formernameChangeDay.map(_ => {
      FormerNameDateView(LocalDate.of(
        data.applicantDetails.formernameChangeYear.getOrElse("1900").toInt,
        data.applicantDetails.formernameChangeMonth.getOrElse("1").toInt,
        data.applicantDetails.formernameChangeDay.getOrElse("1").toInt))
    })

    ApplicantDetails(
      previousAddress = threeYears.map(t => PreviousAddressView(t.toBoolean, previousAddress)),
      homeAddress = homeAddress.map(a => HomeAddressView(a.id, Some(a))),
      emailAddress = Some(EmailAddress(data.applicantDetails.email.getOrElse("test@test.com"))),
      emailVerified = Some(EmailVerified(data.applicantDetails.emailVerified.getOrElse(true))),
      telephoneNumber = Some(TelephoneNumber(data.applicantDetails.phone.getOrElse("12345 123456"))),
      formerName = formerName,
      formerNameDate = formerNameDate
    )
  }
}
