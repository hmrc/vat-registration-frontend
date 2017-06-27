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

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class VatScheme(
                      id: String,
                      tradingDetails: Option[VatTradingDetails] = None,
                      lodgingOfficer: Option[VatLodgingOfficer] = None,
                      financials: Option[VatFinancials] = None,
                      vatSicAndCompliance: Option[VatSicAndCompliance] = None,
                      vatContact: Option[VatContact] = None,
                      vatServiceEligibility: Option[VatServiceEligibility] = None,
                      ppob: Option[ScrsAddress] = None,
                      vatFlatRateSchemeAnswers: Option[VatFlatRateScheme] = None

                    )

object VatScheme {

  implicit val format: OFormat[VatScheme] = (
    (__ \ "registrationId").format[String] and
      (__ \ "tradingDetails").formatNullable[VatTradingDetails] and
      (__ \ "lodgingOfficer").formatNullable[VatLodgingOfficer] and
      (__ \ "financials").formatNullable[VatFinancials] and
      (__ \ "vatSicAndCompliance").formatNullable[VatSicAndCompliance] and
      (__ \ "vatContact").formatNullable[VatContact] and
      (__ \ "vatEligibility").formatNullable[VatServiceEligibility] and
      (__ \ "ppob").formatNullable[ScrsAddress] and
      (__ \ "vatFlatRateSchemeAnswers").formatNullable[VatFlatRateScheme]

    ) (VatScheme.apply, unlift(VatScheme.unapply))

}
