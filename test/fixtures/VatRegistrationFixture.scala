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

package fixtures

import models.api.{VatDetails, VatRegistration}
import models.view.{Summary, SummaryRow, SummarySection}

trait VatRegistrationFixture {

  val validVatDetailsAPI = VatDetails(
    taxableTurnover = Some("No"),
    registerVoluntarily = Some("Yes"),
    startDate = Some("1 February 2017")
  )

  val validVatRegistrationAPI = VatRegistration(
    registrationID = "AC123456",
    formCreationTimestamp = "2017-01-11T15:10:12",
    vatDetails = validVatDetailsAPI
  )

  lazy val validSummaryView = Summary(
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
}
