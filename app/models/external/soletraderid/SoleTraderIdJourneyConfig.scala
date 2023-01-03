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

package models.external.soletraderid

import play.api.libs.json._

case class SoleTraderIdJourneyConfig(continueUrl: String,
                                     deskProServiceId: String,
                                     signOutUrl: String,
                                     accessibilityUrl: String,
                                     regime: String,
                                     businessVerificationCheck: Boolean,
                                     labels: Option[JourneyLabels] = None)

object SoleTraderIdJourneyConfig {
  implicit val format: Format[SoleTraderIdJourneyConfig] = Json.format[SoleTraderIdJourneyConfig]
}

case class JourneyLabels(en: TranslationLabels,
                         cy: TranslationLabels)

object JourneyLabels {
  implicit val format: OFormat[JourneyLabels] = Json.format[JourneyLabels]
}

case class TranslationLabels(optFullNamePageLabel: Option[String] = None,
                             optServiceName: Option[String])

object TranslationLabels {
  implicit val format: OFormat[TranslationLabels] = Json.format[TranslationLabels]
}