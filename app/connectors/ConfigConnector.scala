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

package connectors

import java.util.MissingResourceException
import javax.inject.Inject

import models.api.SicCode
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.config.inject.ServicesConfig

class ConfigConnectorImpl @Inject()(val config: ServicesConfig) extends ConfigConnector

trait ConfigConnector {
  val config: ServicesConfig

  private val sicCodePrefix = "sic.codes"
  private val businessTypesPrefix = "frs.businessTypes"

  lazy val businessTypes: Seq[JsObject] = Json.parse(config.getString(businessTypesPrefix)).as[Seq[JsObject]]

  def getSicCodeDetails(sicCode: String): SicCode = SicCode(
    id             = sicCode,
    description    = config.getString(s"$sicCodePrefix.$sicCode.description"),
    displayDetails = config.getString(s"$sicCodePrefix.$sicCode.displayDetails")
  )

  def getSicCodeFRSCategory(sicCode: String): String = config.getString(s"$sicCodePrefix.$sicCode.frsCategory")

  def getBusinessTypeDetails(id: String): (String, BigDecimal) = {
    val businessType = businessTypes.flatMap { jsObj =>
      (jsObj \ "categories").as[Seq[JsObject]]
    }.find(jsObj => (jsObj \ "id").as[String] == id)

    businessType.fold(throw new MissingResourceException(s"Missing Business Type for id: $id", "ConfigConnector", "id")){ jsObj =>
      ((jsObj \ "businessType").as[String], (jsObj \ "currentFRSPercent").as[BigDecimal])
    }
  }
}
