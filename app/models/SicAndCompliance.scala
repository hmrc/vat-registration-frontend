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
import play.api.libs.json._
import utils.OptionalJsonFields

case class SicAndCompliance(description: Option[BusinessActivityDescription] = None,
                            mainBusinessActivity: Option[MainBusinessActivityView] = None,
                            businessActivities: Option[BusinessActivities] = None,
                            supplyWorkers: Option[SupplyWorkers] = None,
                            workers: Option[Workers] = None,
                            intermediarySupply: Option[IntermediarySupply] = None)

object SicAndCompliance extends OptionalJsonFields {
  val NUMBER_OF_WORKERS_THRESHOLD: Int = 8
  implicit val format: OFormat[SicAndCompliance] = Json.format[SicAndCompliance]
  implicit val s4lKey: S4LKey[SicAndCompliance] = S4LKey("SicAndCompliance")

  def fromApi(json: JsValue): SicAndCompliance = {
    val sicCode = (json \ "mainBusinessActivity").as[SicCode]
    val businessActivities = (json \ "businessActivities").as[List[SicCode]]
    val supplyWorkers = (json \ "labourCompliance" \ "supplyWorkers").validateOpt[Boolean].getOrElse(None)
    val numOfWorkers = (json \ "labourCompliance" \ "numOfWorkersSupplied").validateOpt[Int].getOrElse(None)
    val intermediarySupply = (json \ "labourCompliance" \ "intermediaryArrangement").validateOpt[Boolean].getOrElse(None)

    SicAndCompliance(
      description = Some(BusinessActivityDescription((json \ "businessDescription").as[String])),
      mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.code, mainBusinessActivity = Some(sicCode))),
      businessActivities = Some(BusinessActivities(businessActivities)),
      supplyWorkers = supplyWorkers.map(SupplyWorkers.apply),
      workers = numOfWorkers.map(Workers.apply),
      intermediarySupply = intermediarySupply.map(IntermediarySupply.apply)
    )
  }

  val reads: Reads[SicAndCompliance] = new Reads[SicAndCompliance] {
    override def reads(json: JsValue): JsResult[SicAndCompliance] = {
      JsSuccess(fromApi(json))
    }
  }

  val toApiWrites = new Writes[SicAndCompliance] {
    override def writes(sac: SicAndCompliance): JsValue = {
      val busDesc = Json.obj("businessDescription" ->
        sac.description.map(_.description).getOrElse(throw new IllegalStateException("Missing business description to convert to API model"))
      )
      val businessActivities = Json.obj("businessActivities" ->
        sac.businessActivities.map(_.sicCodes).getOrElse(throw new IllegalStateException("Missing other business activities to convert to API model")))
      val supplyWorkers = sac.supplyWorkers.map(_.yesNo)
      val tempContracts = sac.intermediarySupply.map(_.yesNo)
      val numWorkers = sac.workers.map(_.numberOfWorkers)

      val labour = supplyWorkers.map(supply =>
        Json.obj(
          "labourCompliance" -> Json.obj(
            "supplyWorkers" -> supply
          )
            .++(optional("intermediaryArrangement" -> tempContracts))
            .++(optional("numOfWorkersSupplied" -> numWorkers))
        )
      )

      val mainBus = Json.obj("mainBusinessActivity" ->
        sac.mainBusinessActivity
          .flatMap(_.mainBusinessActivity)
          .map(Json.toJson(_))
          .getOrElse(throw new IllegalStateException("Missing SIC Code to convert to API model"))
      )

      Seq(Some(busDesc), Some(businessActivities), labour, Some(mainBus)).flatten.reduceLeft(_ ++ _)
    }
  }

  val apiFormat = Format[SicAndCompliance](reads, toApiWrites)
}