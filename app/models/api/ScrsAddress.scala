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

package models.api

import play.api.libs.json.{Json, OFormat}

case class ScrsAddress(line1: String,
                       line2: String,
                       line3: Option[String] = None,
                       line4: Option[String] = None,
                       postcode: Option[String] = None, // TODO can we use a mandatory union type i.e. PostcodeOrCountry
                       country: Option[String] = None ) // TODO instead of two optional fields ?
{
  def getId(): String = (line1 + {if (postcode.isDefined) postcode.get else country.get}).replaceAll(" ","")

  def asLabel(): String =
    Seq(line1, line2, line3, line4, postcode, country).collect {
      case s:String => s
      case Some(l) => l
    } mkString (", ")

  override def equals(obj: Any): Boolean = obj match {
    case ScrsAddress(`line1`, _, _, _, `postcode`, `country`) => true
    case _ => false
  }

  override def hashCode: Int = (line1, postcode, country).##
}

object ScrsAddress {
  implicit val format: OFormat[ScrsAddress] = Json.format[ScrsAddress]

}
