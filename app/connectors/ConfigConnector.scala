/*
 * Copyright 2023 HM Revenue & Customs
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

import models.api.Country
import models.{FrsBusinessType, FrsGroup}
import play.api.Environment
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.MissingResourceException
import javax.inject.{Inject, Singleton}
import scala.io.Source
import utils.LoggingUtil
import play.api.mvc.Request

@Singleton
class ConfigConnector @Inject()(val config: ServicesConfig,
                                val environment: Environment) extends LoggingUtil {

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

  def getSicCodeFRSCategory(sicCode: String)(implicit request: Request[_]): String = {
    infoLog(s"Getting FRSCategory for SIC code: $sicCode")
    config.getString(s"$sicCodePrefix.$sicCode")
  }

  def getBusinessType(frsId: String)(implicit request: Request[_]): FrsBusinessType = {
    infoLog(s"Getting Business Type for FRS ID: $frsId")
    val businessType = businessTypes.flatMap(_.categories).find(_.id.equals(frsId))

    businessType.getOrElse(throw new MissingResourceException(s"Missing Business Type for id: $frsId", "ConfigConnector", "id"))
  }
}
