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

import models.S4LFlatRateScheme
import models.api.VatFlatRateScheme
import models.view.frs._
import models.view.frs.AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS

trait FlatRateFixture {

  val validBusinessSectorView = BusinessSectorView("test business sector", 3.14)

  val validS4LFlatRateScheme = S4LFlatRateScheme(
    joinFrs = Some(JoinFrsView(true)),
    annualCostsInclusive = Some(AnnualCostsInclusiveView(YES_WITHIN_12_MONTHS)),
    annualCostsLimited = Some(AnnualCostsLimitedView(YES_WITHIN_12_MONTHS)),
    registerForFrs = Some(RegisterForFrsView(false)),
    categoryOfBusiness = Some(validBusinessSectorView),
    frsStartDate = Some(FrsStartDateView())
  )

  val validVatFlatRateScheme = VatFlatRateScheme(
    joinFrs = true,
    annualCostsInclusive = Some(AnnualCostsInclusiveView.YES_WITHIN_12_MONTHS),
    annualCostsLimited = Some(AnnualCostsLimitedView.YES_WITHIN_12_MONTHS),
    doYouWantToUseThisRate = Some(false),
    categoryOfBusiness = Some(validBusinessSectorView.businessSector),
    percentage = Some(BigDecimal(3.14))
  )
}