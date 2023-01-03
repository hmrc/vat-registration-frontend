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

import models.api._
import play.api.libs.json.{Json, OFormat}

case class Business(hasTradingName: Option[Boolean] = None,
                    tradingName: Option[String] = None,
                    shortOrgName: Option[String] = None,
                    ppobAddress: Option[Address] = None,
                    email: Option[String] = None,
                    telephoneNumber: Option[String] = None,
                    hasWebsite: Option[Boolean] = None,
                    website: Option[String] = None,
                    contactPreference: Option[ContactPreference] = None,
                    welshLanguage: Option[Boolean] = None,
                    hasLandAndProperty: Option[Boolean] = None,
                    businessDescription: Option[String] = None,
                    mainBusinessActivity: Option[SicCode] = None,
                    businessActivities: Option[List[SicCode]] = None,
                    labourCompliance: Option[LabourCompliance] = None,
                    otherBusinessInvolvement: Option[Boolean] = None) {

  lazy val otherBusinessActivities: List[SicCode] =
    businessActivities.getOrElse(Nil).diff(mainBusinessActivity.toList)
}

object Business {
  implicit val format: OFormat[Business] = Json.format[Business]
  implicit val apiKey: ApiKey[Business] = ApiKey("business")

  def tradingNameOptional(partyType: PartyType): Boolean =
    Seq(UkCompany, RegSociety, CharitableOrg, NonUkNonEstablished, Trust, UnincorpAssoc, Partnership, ScotPartnership, ScotLtdPartnership, LtdPartnership, LtdLiabilityPartnership).contains(partyType)
}
