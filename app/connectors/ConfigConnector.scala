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

package connectors

import models.api.{Country, SicCode}
import models.{FrsBusinessType, FrsGroup}
import play.api.Environment
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.MissingResourceException
import javax.inject.{Inject, Singleton}
import scala.io.Source

@Singleton
class ConfigConnector @Inject()(val config: ServicesConfig,
                                val environment: Environment) {

  private val sicCodePrefix = "sic.codes"

  lazy val businessTypes: Seq[FrsGroup] = {
    val frsBusinessTypesFile = "conf/frs-business-types.json"

    val bufferedSource = Source.fromFile(environment.getFile(frsBusinessTypesFile))
    val fileContents = bufferedSource.getLines.mkString
    bufferedSource.close

    (Json.parse(fileContents).as[JsObject] \ "businessTypes").as[Seq[FrsGroup]]
  }

  lazy val countries: Seq[Country] = {
    val countriesFile = "conf/countries-en.json"
    val countriesBuffer = Source.fromFile(environment.getFile(countriesFile))
    val rawJson = countriesBuffer.getLines().mkString
    countriesBuffer.close

    val json = Json.parse(rawJson).as[Map[String, JsValue]]

    json.map {
      case (code, json) => Country(Some(code), (json \ "name").asOpt[String])
    }.toSeq.sortBy(_.name)
  }

  def getSicCodeDetails(sicCode: String): SicCode = {
    val amendedCode = sicCode + "001"

    SicCode(
      code = amendedCode,
      description = config.getString(s"$sicCodePrefix.$amendedCode.description"),
      descriptionCy = config.getString(s"$sicCodePrefix.$amendedCode.description")
    )
  }

  def getSicCodeFRSCategory(sicCode: String): String = config.getString(s"$sicCodePrefix.${sicCode}001.frsCategory")

  def getBusinessType(frsId: String): FrsBusinessType = {
    val businessType = businessTypes.flatMap(_.categories).find(_.id.equals(frsId))

    businessType.getOrElse(throw new MissingResourceException(s"Missing Business Type for id: $frsId", "ConfigConnector", "id"))
  }
}
