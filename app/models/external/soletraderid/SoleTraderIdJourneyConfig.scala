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

package models.external.soletraderid

import play.api.libs.json._

case class SoleTraderIdJourneyConfig(continueUrl: String,
                                     optServiceName: Option[String] = None,
                                     optFullNamePageLabel: Option[String] = None,
                                     deskProServiceId: String,
                                     signOutUrl: String,
                                     accessibilityUrl: String,
                                     regime: String,
                                     businessVerificationCheck: Boolean,
                                     labels: Option[JourneyLabels] = None)

object SoleTraderIdJourneyConfig {
  implicit val format: Format[SoleTraderIdJourneyConfig] = Json.format[SoleTraderIdJourneyConfig]
}

case class JourneyLabels(welsh: TranslationLabels)

object JourneyLabels {

  private val welshLabelsKey: String = "cy"

  implicit val reads: Reads[JourneyLabels] = (JsPath \ welshLabelsKey).read[TranslationLabels].map(JourneyLabels.apply)
  implicit val writes: OWrites[JourneyLabels] = (JsPath \ welshLabelsKey).write[TranslationLabels].contramap(_.welsh)

  val format: OFormat[JourneyLabels] = OFormat(reads, writes)
}

case class TranslationLabels(optFullNamePageLabel: Option[String] = None, optServiceName: Option[String])

object TranslationLabels {

  implicit val format: OFormat[TranslationLabels] = Json.format[TranslationLabels]

}