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

package controllers.builders

import models.BusinessContact
import fixtures.VatRegistrationFixture
import models.{BusinessContact, CompanyContactDetails}
import models.api.ScrsAddress
import models.view.SummaryRow
import testHelpers.VatRegSpec

class SummaryCompanyContactDetailsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "The section builder composing the company contact details section" should {
    val businessContact = BusinessContact(
      companyContactDetails = Some(CompanyContactDetails(
        email           = "some@email.com",
        phoneNumber     = Some("0123456789"),
        mobileNumber    = Some("0123456789"),
        websiteAddress  = Some("http://website.com")
      )),
      ppobAddress = Some(scrsAddress)
    )

    val sectionBuilder = SummaryCompanyContactDetailsSectionBuilder(Some(businessContact))


    "render Business Email row" in {
      sectionBuilder.businessEmailRow mustBe SummaryRow(
        id = "companyContactDetails.email",
        answerMessageKey = "some@email.com",
        changeLink = Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
      )
    }

    "render Business Daytime Phone Number row" in {
      sectionBuilder.businessDaytimePhoneNumberRow mustBe SummaryRow(
        id = "companyContactDetails.daytimePhone",
        answerMessageKey = "0123456789",
        changeLink = Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
      )
    }

    "render Business Mobile Phone Number row" in {
      sectionBuilder.businessMobilePhoneNumberRow mustBe SummaryRow(
        id = "companyContactDetails.mobile",
        answerMessageKey = "0123456789",
        changeLink = Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
      )
    }

    "render Business Website row" in {
      sectionBuilder.businessWebsiteRow mustBe SummaryRow(
        id = "companyContactDetails.website",
        answerMessageKey = "http://website.com",
        changeLink = Some(controllers.routes.BusinessContactDetailsController.showCompanyContactDetails())
      )
    }


    "a real Ppob value should be returned expected test value for Ppob" in {
      val Some(a) = Some(111)
      val b@(x, y) = (1, "2")

      import ScrsAddress.htmlShow._
      val builder = SummaryCompanyContactDetailsSectionBuilder(Some(businessContact))
      builder.ppobRow mustBe
        SummaryRow(
          "companyContactDetails.ppob",
          scrsAddress,
          changeLink = Some(controllers.routes.BusinessContactDetailsController.showPPOB())
        )
    }
  }
}
