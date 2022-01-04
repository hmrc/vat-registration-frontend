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

package models

import models.api.SicCode
import play.api.libs.json.Json

case class BusinessActivityDescription(description: String)
object BusinessActivityDescription {
  implicit val format = Json.format[BusinessActivityDescription]
}

case class MainBusinessActivityView(id: String, mainBusinessActivity: Option[SicCode] = None)
object MainBusinessActivityView {
  def apply(cc: SicCode): MainBusinessActivityView = new MainBusinessActivityView(cc.code, Some(cc))

  implicit val format = Json.format[MainBusinessActivityView]
}

case class BusinessActivities(sicCodes: List[SicCode])

object BusinessActivities {
  implicit val format = Json.format[BusinessActivities]
}
