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

package models.view.sicAndCompliance.labour {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat}
  import play.api.libs.json._

  case class  CompanyProvideWorkers(yesNo: String)

  object CompanyProvideWorkers {

    val PROVIDE_WORKERS_YES = "PROVIDE_WORKERS_YES"
    val PROVIDE_WORKERS_NO = "PROVIDE_WORKERS_NO"

    def toBool(answer:String):Option[Boolean] = if(PROVIDE_WORKERS_YES == answer) Some(true) else Some(false)

    val valid = (item: String) => List(PROVIDE_WORKERS_YES, PROVIDE_WORKERS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[CompanyProvideWorkers]


  }

  case class  SkilledWorkers(yesNo: String)

  object SkilledWorkers {
    def toBool(answer:String):Option[Boolean] = if(SKILLED_WORKERS_YES == answer) Some(true) else Some(false)

    val SKILLED_WORKERS_YES = "SKILLED_WORKERS_YES"
    val SKILLED_WORKERS_NO = "SKILLED_WORKERS_NO"

    val valid = (item: String) => List(SKILLED_WORKERS_YES, SKILLED_WORKERS_NO).contains(item.toUpperCase)


    implicit val format = Json.format[SkilledWorkers]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LVatSicAndCompliance) => group.skilledWorkers,
//      updateF = (c: SkilledWorkers, g: Option[S4LVatSicAndCompliance]) =>
//        g.getOrElse(S4LVatSicAndCompliance()).copy(skilledWorkers = Some(c))
//    )
//
//    implicit val modelTransformer = ApiModelTransformer[SkilledWorkers] { (vs: VatScheme) =>
//      for {
//        vsc <- vs.vatSicAndCompliance
//        lc <- vsc.labourCompliance
//        sw <- lc.skilledWorkers
//      } yield SkilledWorkers(if (sw) SKILLED_WORKERS_YES else SKILLED_WORKERS_NO)
//    }

  }

  case class TemporaryContracts(yesNo: String)

  object TemporaryContracts {
    def toBool(answer:String) = if(TEMP_CONTRACTS_YES == answer) Some(true) else Some(false)

    val TEMP_CONTRACTS_YES = "TEMP_CONTRACTS_YES"
    val TEMP_CONTRACTS_NO = "TEMP_CONTRACTS_NO"

    val valid = (item: String) => List(TEMP_CONTRACTS_YES, TEMP_CONTRACTS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[TemporaryContracts]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LVatSicAndCompliance) => group.temporaryContracts,
//      updateF = (c: TemporaryContracts, g: Option[S4LVatSicAndCompliance]) =>
//        g.getOrElse(S4LVatSicAndCompliance()).copy(temporaryContracts = Some(c))
//    )
//
//    implicit val modelTransformer = ApiModelTransformer[TemporaryContracts] { (vs: VatScheme) =>
//      for {
//        vsc <- vs.vatSicAndCompliance
//        lc <- vsc.labourCompliance
//        tc <- lc.temporaryContracts
//      } yield TemporaryContracts(if (tc) TEMP_CONTRACTS_YES else TEMP_CONTRACTS_NO)
//    }

  }

  case class Workers(numberOfWorkers: Int)

  object Workers {

    implicit val format: OFormat[Workers] = Json.format[Workers]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LVatSicAndCompliance) => group.workers,
//      updateF = (c: Workers, g: Option[S4LVatSicAndCompliance]) =>
//        g.getOrElse(S4LVatSicAndCompliance()).copy(workers = Some(c))
//    )
//
//    implicit val modelTransformer = ApiModelTransformer[Workers] { (vs: VatScheme) =>
//      for {
//        vsc <- vs.vatSicAndCompliance
//        lc <- vsc.labourCompliance
//        w <- lc.workers
//      } yield Workers(w)
//    }

  }
}

package models.view.sicAndCompliance {

  import models.api.{VatScheme, _}
  import models.{ApiModelTransformer, _}
  import play.api.libs.json.Json

  case class BusinessActivityDescription(description: String)

  object BusinessActivityDescription {

    implicit val format = Json.format[BusinessActivityDescription]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LVatSicAndCompliance) => group.description,
//      updateF = (c: BusinessActivityDescription, g: Option[S4LVatSicAndCompliance]) =>
//        g.getOrElse(S4LVatSicAndCompliance()).copy(description = Some(c))
//    )
//
//    implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
//      vs.vatSicAndCompliance.map(_.businessDescription).collect {
//        case description => BusinessActivityDescription(description)
//      }
//    }

  }

  case class MainBusinessActivityView(id: String, mainBusinessActivity: Option[SicCode] = None)

  object MainBusinessActivityView {

    def apply(cc: SicCode): MainBusinessActivityView = new MainBusinessActivityView(cc.id, Some(cc))

    implicit val format = Json.format[MainBusinessActivityView]

//    implicit val viewModelFormat = ViewModelFormat(
//      readF = (group: S4LVatSicAndCompliance) => group.mainBusinessActivity,
//      updateF = (c: MainBusinessActivityView, g: Option[S4LVatSicAndCompliance]) =>
//        g.getOrElse(S4LVatSicAndCompliance()).copy(mainBusinessActivity = Some(c))
//    )
//
//    // return a view model from a VatScheme instance
//    implicit val modelTransformer = ApiModelTransformer[MainBusinessActivityView] { vs: VatScheme =>
//      vs.vatSicAndCompliance.map(cc =>
//        MainBusinessActivityView(cc.mainBusinessActivity.id,
//          Some(SicCode(cc.mainBusinessActivity.id, cc.mainBusinessActivity.description, cc.mainBusinessActivity.displayDetails))))
//    }

  }

}
