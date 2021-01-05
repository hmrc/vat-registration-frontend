/*
 * Copyright 2021 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import models.api.SicCode
import play.api.Environment
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.io.Source

@Singleton
class ConfigConnector @Inject()(val config: ServicesConfig,
                                val environment: Environment) {

  private val sicCodePrefix = "sic.codes"

  lazy val businessTypes: Seq[JsObject] = {
    val frsBusinessTypesFile = "conf/frs-business-types.json"

    val bufferedSource = Source.fromFile(environment.getFile(frsBusinessTypesFile))
    val fileContents = bufferedSource.getLines.mkString
    bufferedSource.close

    val json = Json.parse(fileContents).as[JsObject]
    (json \ "businessTypes").as[Seq[JsObject]]
  }

  def getSicCodeDetails(sicCode: String): SicCode = {
    val amendedCode = sicCode + "001"

    SicCode(
      code = amendedCode,
      description = config.getString(s"$sicCodePrefix.$amendedCode.description"),
      displayDetails = config.getString(s"$sicCodePrefix.$amendedCode.displayDetails")
    )
  }

  def getSicCodeFRSCategory(sicCode: String): String = config.getString(s"$sicCodePrefix.${sicCode}001.frsCategory")

  def getBusinessTypeDetails(frsId: String): (String, BigDecimal) = {
    val businessType = businessTypes.flatMap { jsObj =>
      (jsObj \ "categories").as[Seq[JsObject]]
    }.find(jsObj => (jsObj \ "id").as[String] == frsId)

    businessType.fold(throw new MissingResourceException(s"Missing Business Type for id: $frsId", "ConfigConnector", "id")) { jsObj =>
      ((jsObj \ "businessType").as[String], (jsObj \ "currentFRSPercent").as[BigDecimal])
    }
  }
}
