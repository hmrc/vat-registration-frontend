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

package services

import helpers.VatRegSpec
import models.api.{VatDetails, VatRegistration => VatRegistrationAPI}
import models.view.{Summary, SummaryRow, SummarySection}

import scala.concurrent.ExecutionContext.Implicits.global

class VatRegistrationServiceSpec extends VatRegSpec {

  val apiRegistration = VatRegistrationAPI(
    registrationID = "VAT123456",
    formCreationTimestamp = "2017-01-11T15:10:12",
    vatDetails = VatDetails(
      taxableTurnover = Some("No"),
      registerVoluntarily = Some("Yes"),
      startDate = Some("1 February 2017")
    )
  )

  lazy val summary = Summary(
    Seq(SummarySection(
      id = "vatDetails",
      Seq(SummaryRow(
        id = "vatDetails.taxableTurnover",
        answer = Right("No"),
        changeLink = Some(controllers.userJourney.routes.TaxableTurnoverController.show())
      ),
        SummaryRow(
          id = "vatDetails.registerVoluntarily",
          answer = Right("Yes"),
          changeLink = Some(controllers.userJourney.routes.SummaryController.show())
        ),
        SummaryRow(
          id = "vatDetails.startDate",
          answer = Right("1 February 2017"),
          changeLink = Some(controllers.userJourney.routes.StartDateController.show())
        ))
    ))
  )

  "convert a VAT Registration API Model to a summary model with VAT details" should {
    "return success" in {
      vatRegistrationService.registrationToSummary(apiRegistration) mustBe summary
    }
  }

  "get a registration summary" should {
    "return success" in {
      vatRegistrationService.getRegistrationSummary()
    }
  }

}
