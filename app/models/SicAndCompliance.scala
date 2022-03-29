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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.OptionalJsonFields

case class SicAndCompliance(description: Option[BusinessActivityDescription] = None,
                            mainBusinessActivity: Option[MainBusinessActivityView] = None,
                            businessActivities: Option[BusinessActivities] = None,
                            supplyWorkers: Option[SupplyWorkers] = None,
                            workers: Option[Workers] = None,
                            intermediarySupply: Option[IntermediarySupply] = None,
                            hasLandAndProperty: Option[Boolean] = None,
                            otherBusinessInvolvement: Option[Boolean] = None)

object SicAndCompliance extends OptionalJsonFields {
  val NUMBER_OF_WORKERS_THRESHOLD: Int = 8
  implicit val format: OFormat[SicAndCompliance] = Json.format[SicAndCompliance]
  implicit val s4lKey: S4LKey[SicAndCompliance] = S4LKey("SicAndCompliance")

  val reads: Reads[SicAndCompliance] = (
    (__ \ "businessDescription").readNullable[String].fmap(_.map(BusinessActivityDescription(_))) and
      (__ \ "mainBusinessActivity").readNullable[SicCode].fmap(_.map(sicCode => {
        MainBusinessActivityView(id = sicCode.code, mainBusinessActivity = Some(sicCode))
      })) and
      (__ \ "businessActivities").readNullable[List[SicCode]].fmap(_.map(BusinessActivities(_))) and
      (__ \ "labourCompliance" \ "supplyWorkers").readNullable[Boolean].fmap(_.map(SupplyWorkers(_))) and
      (__ \ "labourCompliance" \ "numOfWorkersSupplied").readNullable[Int].fmap(_.map(Workers(_))) and
      (__ \ "labourCompliance" \ "intermediaryArrangement").readNullable[Boolean].fmap(_.map(IntermediarySupply(_))) and
      (__ \ "hasLandAndProperty").readNullable[Boolean] and
      (__ \ "otherBusinessInvolvement").readNullable[Boolean]
    ) (SicAndCompliance.apply _)

  val toApiWrites: Writes[SicAndCompliance] = (
    (__ \ "businessDescription").writeNullable[String].contramap[Option[BusinessActivityDescription]](
      _.map(_.description).orElse(throw new IllegalStateException("Missing business description to convert to API model"))
    ) and
      (__ \ "mainBusinessActivity").writeNullable[SicCode].contramap[Option[MainBusinessActivityView]] (
        _.flatMap(_.mainBusinessActivity).orElse(throw new IllegalStateException("Missing SIC Code to convert to API model"))
      ) and
      (__ \ "businessActivities").writeNullable[List[SicCode]].contramap[Option[BusinessActivities]](
        _.map(_.sicCodes).orElse(throw new IllegalStateException("Missing other business activities to convert to API model"))
      ) and
      (__ \ "labourCompliance" \ "supplyWorkers").writeNullable[Boolean].contramap[Option[SupplyWorkers]](_.map(_.yesNo)) and
      (__ \ "labourCompliance" \ "numOfWorkersSupplied").writeNullable[Int].contramap[Option[Workers]](_.map(_.numberOfWorkers)) and
      (__ \ "labourCompliance" \ "intermediaryArrangement").writeNullable[Boolean].contramap[Option[IntermediarySupply]](_.map(_.yesNo)) and
      (__ \ "hasLandAndProperty").writeNullable[Boolean] and
      (__ \ "otherBusinessInvolvement").writeNullable[Boolean]
    ) (unlift(SicAndCompliance.unapply))

  val apiFormat: Format[SicAndCompliance] = Format[SicAndCompliance](reads, toApiWrites)
}