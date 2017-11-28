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

package connectors

import javax.inject.{Inject, Singleton}

import models.api.SicCode
import models.view.frs.BusinessSectorView
import uk.gov.hmrc.play.config.inject.ServicesConfig

@Singleton
class ConfigConnector @Inject()(config: ServicesConfig) {
  private val sicCodePrefix = "sic.codes"

  def getSicCodeDetails(sicCode: String): SicCode = SicCode(
    id             = sicCode,
    description    = config.getString(s"$sicCodePrefix.$sicCode.description"),
    displayDetails = config.getString(s"$sicCodePrefix.$sicCode.displayDetails")
  )

  def getBusinessSectorDetails(sicCode: String): BusinessSectorView = BusinessSectorView(
    businessSector      = config.getString(s"$sicCodePrefix.$sicCode.frsCategory"),
    flatRatePercentage  = BigDecimal(config.getString(s"$sicCodePrefix.$sicCode.currentFRSPercent"))
  )
}
