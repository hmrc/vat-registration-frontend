/*
 * Copyright 2022 HM Revenue & Customs
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

package viewmodels.taslkist

import fixtures.VatRegistrationFixture
import models.Business
import testHelpers.VatRegSpec
import viewmodels.tasklist._

class AboutTheBusinessTaskListSpec extends VatRegSpec with VatRegistrationFixture {

  val section: AboutTheBusinessTaskList = app.injector.instanceOf[AboutTheBusinessTaskList]

  "The business details row" must {
    "be cannot started if the prerequesites are not complete" in {
      val scheme = emptyVatScheme

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLCannotStart
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be not started if the prerequesites are complete but there are no answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails)
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLNotStarted
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be in progress if the prerequesites are complete and there are some answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails),
        business = Some(Business(ppobAddress = Some(testAddress)))
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLInProgress
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }

    "be complete if the prerequesites are complete and there are all answers" in {
      val scheme = emptyVatScheme.copy(
        eligibilitySubmissionData = Some(validEligibilitySubmissionData),
        applicantDetails = Some(completeApplicantDetails),
        business = Some(validBusiness),
        tradingDetails = Some(generateTradingDetails())
      )

      val row = section.businessDetailsRow.build(scheme)

      row.status mustBe TLCompleted
      row.url mustBe controllers.routes.TradingNameResolverController.resolve.url
    }
  }

}
