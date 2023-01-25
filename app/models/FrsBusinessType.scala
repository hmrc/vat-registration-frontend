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

import config.FrontendAppConfig
import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class FrsGroup(label: String, labelCy: String, categories: List[FrsBusinessType]) {
  def groupLabel(implicit messages: Messages, appConfig: FrontendAppConfig): String = messages.lang.code match {
    case "cy" => labelCy
    case _ => label
  }
}

case class FrsBusinessType(id: String, label: String, labelCy: String, percentage: BigDecimal) {
  def businessTypeLabel(implicit messages: Messages, appConfig: FrontendAppConfig): String = messages.lang.code match {
    case "cy" => labelCy
    case _ => label
  }
}

object FrsGroup {
  implicit val reads: Reads[FrsGroup] =
    (
      (__ \ "groupLabel").read[String] and
        (__ \ "groupLabelCy").read[String] and
        (__ \ "categories").read[List[FrsBusinessType]]
      ) (FrsGroup.apply _)
}

object FrsBusinessType {
  implicit val reads: Reads[FrsBusinessType] =
    (
      (__ \ "id").read[String] and
        (__ \ "businessType").read[String] and
        (__ \ "businessTypeCy").read[String] and
        (__ \ "currentFRSPercent").read[BigDecimal]
      ) (FrsBusinessType.apply _)
}