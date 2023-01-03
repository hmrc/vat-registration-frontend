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

package models

import play.api.libs.json.{Json, OFormat}

case class OtherBusinessInvolvement(businessName: Option[String] = None,
                                    hasVrn: Option[Boolean] = None,
                                    vrn: Option[String] = None,
                                    hasUtr: Option[Boolean] = None,
                                    utr: Option[String] = None,
                                    stillTrading: Option[Boolean] = None) {
  def isModelComplete: Boolean =
    this match {
      case OtherBusinessInvolvement(Some(_), Some(true), Some(_), None, None, Some(_)) => true
      case OtherBusinessInvolvement(Some(_), Some(false), None, Some(true), Some(_), Some(_)) => true
      case OtherBusinessInvolvement(Some(_), Some(false), None, Some(false), None, Some(_)) => true
      case _ => false
    }
}

object OtherBusinessInvolvement {
  implicit val format: OFormat[OtherBusinessInvolvement] = Json.format[OtherBusinessInvolvement]
  implicit val apiKey: ApiKey[OtherBusinessInvolvement] = ApiKey("other-business-involvements")

  val minIndex = 1
}