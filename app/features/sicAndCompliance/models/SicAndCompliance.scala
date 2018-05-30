/*
 * Copyright 2018 HM Revenue & Customs
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

package features.sicAndCompliance.models

import models.api.SicCode
import TemporaryContracts.{TEMP_CONTRACTS_NO, TEMP_CONTRACTS_YES}
import SkilledWorkers.{SKILLED_WORKERS_NO, SKILLED_WORKERS_YES}
import CompanyProvideWorkers.{PROVIDE_WORKERS_NO, PROVIDE_WORKERS_YES}
import features.tradingDetails.{TradingDetails, TradingNameView}
import models.S4LKey
import play.api.libs.json
import play.api.libs.json._

case class SicAndCompliance(description: Option[BusinessActivityDescription] = None,
                            mainBusinessActivity: Option[MainBusinessActivityView] = None,
                            otherBusinessActivities: Option[OtherBusinessActivities] = None,
                            //Labour Compliancez
                            companyProvideWorkers: Option[CompanyProvideWorkers] = None,
                            workers: Option[Workers] = None,
                            temporaryContracts: Option[TemporaryContracts] = None,
                            skilledWorkers: Option[SkilledWorkers] = None)

object SicAndCompliance {
  val NUMBER_OF_WORKERS_THRESHOLD: Int = 8
  implicit val format: OFormat[SicAndCompliance] = Json.format[SicAndCompliance]
  implicit val sicAndCompliance: S4LKey[SicAndCompliance] = S4LKey("SicAndCompliance")

  def fromApi(json: JsValue): SicAndCompliance = {

    val sicCode = (json \ "mainBusinessActivity").as[SicCode]
    val otherBusinessActivities = (json \ "otherBusinessActivities").as[List[SicCode]]
    val labourComp = (json \ "labourCompliance").validateOpt[JsObject].get
    val numOfWorkers = labourComp.map(a => (a \ "numberOfWorkers").as[Int])
    val workers = numOfWorkers.flatMap(num => if (num == 0) None else Some(Workers(num)))
    val temporaryContracts = workers.flatMap { _ =>
      (json \ "labourCompliance" \ "temporaryContracts").validateOpt[Boolean].get.map { b =>
        if (b) TemporaryContracts(TEMP_CONTRACTS_YES) else TemporaryContracts(TEMP_CONTRACTS_NO)
      }
    }
    val skilledWorkers = workers.flatMap { a =>
      (json \ "labourCompliance" \ "skilledWorkers").validateOpt[Boolean].get.map { b =>
        if (b) SkilledWorkers(SKILLED_WORKERS_YES) else SkilledWorkers(SKILLED_WORKERS_NO)
      }
    }

    SicAndCompliance(
      description = Some(BusinessActivityDescription((json \ "businessDescription").as[String])),
      mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.code, mainBusinessActivity = Some(sicCode))),
      otherBusinessActivities = Some(OtherBusinessActivities(otherBusinessActivities)),
      companyProvideWorkers = numOfWorkers.map(n => CompanyProvideWorkers(if (n == 0) PROVIDE_WORKERS_NO else PROVIDE_WORKERS_YES)),
      workers = workers,
      temporaryContracts = temporaryContracts,
      skilledWorkers = skilledWorkers
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
      val otherBusinessActivities = Json.obj("otherBusinessActivities" ->
        sac.otherBusinessActivities.map(_.sicCodes).getOrElse(throw new IllegalStateException("Missing other business activities to convert to API model")))
      val provideWorkers = sac.companyProvideWorkers.map(_.yesNo).map(_ == PROVIDE_WORKERS_YES)
      val numWorkers: Int = if (provideWorkers.contains(true)) {
        sac.workers.map(_.numberOfWorkers).getOrElse(throw new IllegalStateException("Missing number of workers to convert to API model"))
      } else {
        0
      }
      val tempContracts: Option[Boolean] = if (numWorkers >= NUMBER_OF_WORKERS_THRESHOLD) sac.temporaryContracts.map(_.yesNo == TEMP_CONTRACTS_YES) else None
      val skill: Option[Boolean] = if (tempContracts.contains(true)) sac.skilledWorkers.map(_.yesNo == SKILLED_WORKERS_YES) else None

      val numberOfWorkers = Json.obj("numberOfWorkers" -> numWorkers)
      val temporaryCont = tempContracts.map(v => Json.obj("temporaryContracts" -> v))
      val skilledWork = skill.map(v => Json.obj("skilledWorkers" -> v))

      val labour = provideWorkers.map(_ => Json.obj("labourCompliance" -> Seq(Some(numberOfWorkers), temporaryCont, skilledWork).flatten.reduceLeft(_ ++ _)))

      val mainBus = Json.obj("mainBusinessActivity" ->
        sac.mainBusinessActivity
          .flatMap(_.mainBusinessActivity)
          .map(Json.toJson(_))
          .getOrElse(throw new IllegalStateException("Missing SIC Code to convert to API model"))
      )

      Seq(Some(busDesc),Some(otherBusinessActivities), labour, Some(mainBus)).flatten.reduceLeft(_ ++ _)
    }
  }

  val apiFormat = Format[SicAndCompliance](reads, toApiWrites)
}