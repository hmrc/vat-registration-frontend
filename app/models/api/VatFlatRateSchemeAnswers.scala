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

case class VatFlatRateSchemeAnswers(joinFrs: Option[Boolean] = None,
                                    annualCostsInclusive: Option[String] = None,
                                    annualCostsLimited: Option[AnnualCostsLimited] = None,
                                    doYouWantToUseThisRate: Option[Boolean] = None,
                                    whenDoYouWantToJoinFrs: Option[String] = None)

object VatFlatRateSchemeAnswers {

  implicit val format = (
    (__ \ "joinFrs").formatNullable[Boolean] and
      (__ \ "annualCostsInclusive").formatNullable[String] and
      (__ \ "annualCostsLimited").formatNullable[AnnualCostsLimited] and
      (__ \ "doYouWantToUseThisRate").formatNullable[Boolean] and
      (__ \ "whenDoYouWantToJoinFrs").formatNullable[String]
    ) (VatFlatRateSchemeAnswers.apply, unlift(VatFlatRateSchemeAnswers.unapply))

}