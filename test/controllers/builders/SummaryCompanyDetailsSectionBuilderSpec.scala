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

package controllers.builders

import helpers.VatRegSpec
import models.api.{SicAndCompliance, VatFinancials}
import models.view.SummaryRow

class SummaryCompanyDetailsSectionBuilderSpec extends VatRegSpec {

  "The section builder composing a company details section" should {

    "with estimatedSalesValueRow render" should {

      "a 'No' if it's a voluntary registration" in {
        val builder = SummaryCompanyDetailsSectionBuilder(VatFinancials.empty, SicAndCompliance())
        builder.estimatedSalesValueRow mustBe
          SummaryRow(
            "companyDetails.estimatedSalesValue",
            "£0",
            Some(controllers.userJourney.routes.EstimateVatTurnoverController.show())
          )
      }
    }


  }

}