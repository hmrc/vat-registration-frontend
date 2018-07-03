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

package controllers.builders

import fixtures.VatRegistrationFixture
import frs.FlatRateScheme
import helpers.VatRegSpec
import models.TurnoverEstimates

class SummaryFrsSectionBuilderSpec extends VatRegSpec with VatRegistrationFixture {

  "summary builder should build frs summary with data if frs present" in {
    val frs = Some(FlatRateScheme(Some(true),Some(true),Some(5003L),Some(true),Some(true),None,None))
    val builder = SummaryFrsSectionBuilder(frs,Some(5000L),Some("Foo Bar Wizz Bang"),Some(TurnoverEstimates(100L)))

    builder.section.rows.length mustBe 7
    builder.joinFrsRow.answerMessageKeys.head mustBe "app.common.yes"
    builder.costsInclusiveRow.answerMessageKeys.head mustBe "app.common.yes"
    builder.estimateTotalSalesRow.answerMessageKeys.head mustBe "£5,003"
    builder.costsLimitedRow.answerMessageKeys.head mustBe "app.common.yes"
    builder.startDateRow.answerMessageKeys.head mustBe ""
    builder.flatRatePercentageRow.answerMessageKeys.head mustBe "app.common.yes"
    builder.businessSectorRow.answerMessageKeys.head mustBe "Foo Bar Wizz Bang"
  }
}
