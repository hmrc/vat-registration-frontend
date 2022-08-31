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

package models.external.partnershipid

import play.api.libs.json._

case class PartnershipIdJourneyConfig(continueUrl: String,
                                      optServiceName: Option[String] = None,
                                      deskProServiceId: String,
                                      signOutUrl: String,
                                      accessibilityUrl: String,
                                      regime: String,
                                      businessVerificationCheck: Boolean,
                                      labels: Option[JourneyLabels] = None)

object PartnershipIdJourneyConfig {
  implicit val format: OFormat[PartnershipIdJourneyConfig] = Json.format[PartnershipIdJourneyConfig]
}

case class JourneyLabels(optWelshServiceName: Option[String])

object JourneyLabels {

  val welshLabelsKey: String = "cy"
  val optServiceNameKey: String = "optServiceName"

  implicit val reads: Reads[JourneyLabels] = (JsPath \ welshLabelsKey \ optServiceNameKey).readNullable[String].map(JourneyLabels.apply)
  implicit val writes: OWrites[JourneyLabels] = (JsPath \ welshLabelsKey \ optServiceNameKey).writeNullable[String].contramap(_.optWelshServiceName)

  val format: OFormat[JourneyLabels] = OFormat(reads, writes)
}