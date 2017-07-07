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

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import models.api.SicCode
import models.view.frs.BusinessSectorView
import uk.gov.hmrc.play.config.ServicesConfig


@Singleton
class ConfigConnector extends ConfigConnect with ServicesConfig {

  val sicCodePrefix = "sic.codes"

  override def getSicCodeDetails(sicCode: String): SicCode =
    SicCode(id = sicCode,
      description = getString(s"$sicCodePrefix.$sicCode.description"),
      displayDetails = getString(s"$sicCodePrefix.$sicCode.displayDetails"))

  def getBusinessSectorDetails(sicCode: String): BusinessSectorView =
    BusinessSectorView(
      businessSector = getString(s"$sicCodePrefix.$sicCode.frsCategory"),
      flatRatePercentage = BigDecimal(getString(s"$sicCodePrefix.$sicCode.currentFRSPercent")))

}

@ImplementedBy(classOf[ConfigConnector])
trait ConfigConnect {

  def getSicCodeDetails(sicCode: String): SicCode

  def getBusinessSectorDetails(sicCode: String): BusinessSectorView

}