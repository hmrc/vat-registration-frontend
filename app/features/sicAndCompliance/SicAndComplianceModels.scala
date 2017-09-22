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

package models.view.sicAndCompliance.cultural {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat}
  import play.api.libs.json.Json

  case class NotForProfit(yesNo: String)

  object NotForProfit {

    val NOT_PROFIT_YES = "NOT_PROFIT_YES"
    val NOT_PROFIT_NO = "NOT_PROFIT_NO"

    val valid = (item: String) => List(NOT_PROFIT_YES, NOT_PROFIT_NO).contains(item.toUpperCase)

    implicit val format = Json.format[NotForProfit]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.notForProfit,
      updateF = (c: NotForProfit, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(notForProfit = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[NotForProfit] { (vs: VatScheme) =>
      vs.vatSicAndCompliance.flatMap(_.culturalCompliance).map { q1 =>
        NotForProfit(if (q1.notForProfit) NOT_PROFIT_YES else NOT_PROFIT_NO)
      }
    }

  }

}

package models.view.sicAndCompliance.financial {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat}
  import play.api.libs.json.Json

  case class ActAsIntermediary(yesNo: Boolean)

  object ActAsIntermediary {

    implicit val format = Json.format[ActAsIntermediary]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.actAsIntermediary,
      updateF = (c: ActAsIntermediary, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(actAsIntermediary = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[ActAsIntermediary] { vs: VatScheme =>
      vs.vatSicAndCompliance.flatMap(_.financialCompliance).map { financialCompliance =>
        ActAsIntermediary(financialCompliance.actAsIntermediary)
      }
    }

  }

  case class AdditionalNonSecuritiesWork(yesNo: Boolean)

  object AdditionalNonSecuritiesWork {

    implicit val format = Json.format[AdditionalNonSecuritiesWork]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.additionalNonSecuritiesWork,
      updateF = (c: AdditionalNonSecuritiesWork, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(additionalNonSecuritiesWork = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[AdditionalNonSecuritiesWork] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        answ <- fc.additionalNonSecuritiesWork
      } yield AdditionalNonSecuritiesWork(answ)
    }

  }

  case class AdviceOrConsultancy(yesNo: Boolean)

  object AdviceOrConsultancy {

    implicit val format = Json.format[AdviceOrConsultancy]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.adviceOrConsultancy,
      updateF = (c: AdviceOrConsultancy, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(adviceOrConsultancy = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[AdviceOrConsultancy] { vs: VatScheme =>
      vs.vatSicAndCompliance.flatMap(_.financialCompliance).map { financialCompliance =>
        AdviceOrConsultancy(financialCompliance.adviceOrConsultancyOnly)
      }
    }

  }

  case class ChargeFees(yesNo: Boolean)

  object ChargeFees {

    implicit val format = Json.format[ChargeFees]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.chargeFees,
      updateF = (c: ChargeFees, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(chargeFees = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[ChargeFees] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        cf <- fc.chargeFees
      } yield ChargeFees(cf)
    }

  }


  case class DiscretionaryInvestmentManagementServices(yesNo: Boolean)

  object DiscretionaryInvestmentManagementServices {

    implicit val format = Json.format[DiscretionaryInvestmentManagementServices]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.discretionaryInvestmentManagementServices,
      updateF = (c: DiscretionaryInvestmentManagementServices, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(discretionaryInvestmentManagementServices = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[DiscretionaryInvestmentManagementServices] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        dims <- fc.discretionaryInvestmentManagementServices
      } yield DiscretionaryInvestmentManagementServices(dims)
    }

  }

  case class InvestmentFundManagement(yesNo: Boolean)

  object InvestmentFundManagement {

    implicit val format = Json.format[InvestmentFundManagement]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.investmentFundManagement,
      updateF = (c: InvestmentFundManagement, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(investmentFundManagement = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[InvestmentFundManagement] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        cf <- fc.investmentFundManagementServices
      } yield InvestmentFundManagement(cf)
    }

  }

  case class LeaseVehicles(yesNo: Boolean)

  object LeaseVehicles {

    implicit val format = Json.format[LeaseVehicles]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.leaseVehicles,
      updateF = (c: LeaseVehicles, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(leaseVehicles = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[LeaseVehicles] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        vel <- fc.vehicleOrEquipmentLeasing
      } yield LeaseVehicles(vel)
    }

  }

  case class ManageAdditionalFunds(yesNo: Boolean)

  object ManageAdditionalFunds {

    implicit val format = Json.format[ManageAdditionalFunds]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.manageAdditionalFunds,
      updateF = (c: ManageAdditionalFunds, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(manageAdditionalFunds = Some(c))
    )

    // Returns a view model for a specific part of a given VatScheme API model
    implicit val modelTransformer = ApiModelTransformer[ManageAdditionalFunds] { vs: VatScheme =>
      for {
        vsc <- vs.vatSicAndCompliance
        fc <- vsc.financialCompliance
        maf <- fc.manageFundsAdditional
      } yield ManageAdditionalFunds(maf)
    }

  }

}

package models.view.sicAndCompliance.labour {

  import models.api.VatScheme
  import models.{ApiModelTransformer, S4LVatSicAndCompliance, ViewModelFormat}
  import play.api.libs.json.{Json, OFormat}

  case class  CompanyProvideWorkers(yesNo: String)

  object CompanyProvideWorkers {

    val PROVIDE_WORKERS_YES = "PROVIDE_WORKERS_YES"
    val PROVIDE_WORKERS_NO = "PROVIDE_WORKERS_NO"

    val valid = (item: String) => List(PROVIDE_WORKERS_YES, PROVIDE_WORKERS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[CompanyProvideWorkers]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.companyProvideWorkers,
      updateF = (c: CompanyProvideWorkers, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(companyProvideWorkers = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[CompanyProvideWorkers] { (vs: VatScheme) =>
      vs.vatSicAndCompliance.flatMap(_.labourCompliance).map { labourCompliance =>
        CompanyProvideWorkers(if (labourCompliance.labour) PROVIDE_WORKERS_YES else PROVIDE_WORKERS_NO)
      }
    }

  }

  case class  SkilledWorkers(yesNo: String)

  object SkilledWorkers {

    val SKILLED_WORKERS_YES = "SKILLED_WORKERS_YES"
    val SKILLED_WORKERS_NO = "SKILLED_WORKERS_NO"

    val valid = (item: String) => List(SKILLED_WORKERS_YES, SKILLED_WORKERS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[SkilledWorkers]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.skilledWorkers,
      updateF = (c: SkilledWorkers, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(skilledWorkers = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[SkilledWorkers] { (vs: VatScheme) =>
      for {
        vsc <- vs.vatSicAndCompliance
        lc <- vsc.labourCompliance
        sw <- lc.skilledWorkers
      } yield SkilledWorkers(if (sw) SKILLED_WORKERS_YES else SKILLED_WORKERS_NO)
    }

  }

  case class TemporaryContracts(yesNo: String)

  object TemporaryContracts {

    val TEMP_CONTRACTS_YES = "TEMP_CONTRACTS_YES"
    val TEMP_CONTRACTS_NO = "TEMP_CONTRACTS_NO"

    val valid = (item: String) => List(TEMP_CONTRACTS_YES, TEMP_CONTRACTS_NO).contains(item.toUpperCase)

    implicit val format = Json.format[TemporaryContracts]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.temporaryContracts,
      updateF = (c: TemporaryContracts, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(temporaryContracts = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[TemporaryContracts] { (vs: VatScheme) =>
      for {
        vsc <- vs.vatSicAndCompliance
        lc <- vsc.labourCompliance
        tc <- lc.temporaryContracts
      } yield TemporaryContracts(if (tc) TEMP_CONTRACTS_YES else TEMP_CONTRACTS_NO)
    }

  }

  case class Workers(numberOfWorkers: Int)

  object Workers {

    implicit val format: OFormat[Workers] = Json.format[Workers]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.workers,
      updateF = (c: Workers, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(workers = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer[Workers] { (vs: VatScheme) =>
      for {
        vsc <- vs.vatSicAndCompliance
        lc <- vsc.labourCompliance
        w <- lc.workers
      } yield Workers(w)
    }

  }
}

package models.view.sicAndCompliance {

  import models.api._
  import models.{ApiModelTransformer, _}
  import models.api.VatScheme
  import play.api.libs.json.Json

  case class BusinessActivityDescription(description: String)

  object BusinessActivityDescription {

    implicit val format = Json.format[BusinessActivityDescription]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.description,
      updateF = (c: BusinessActivityDescription, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(description = Some(c))
    )

    implicit val modelTransformer = ApiModelTransformer { (vs: VatScheme) =>
      vs.vatSicAndCompliance.map(_.businessDescription).collect {
        case description => BusinessActivityDescription(description)
      }
    }

  }

  case class MainBusinessActivityView(id: String, mainBusinessActivity: Option[SicCode] = None)

  object MainBusinessActivityView {

    def apply(cc: SicCode): MainBusinessActivityView = new MainBusinessActivityView(cc.id, Some(cc))

    implicit val format = Json.format[MainBusinessActivityView]

    implicit val viewModelFormat = ViewModelFormat(
      readF = (group: S4LVatSicAndCompliance) => group.mainBusinessActivity,
      updateF = (c: MainBusinessActivityView, g: Option[S4LVatSicAndCompliance]) =>
        g.getOrElse(S4LVatSicAndCompliance()).copy(mainBusinessActivity = Some(c))
    )

    // return a view model from a VatScheme instance
    implicit val modelTransformer = ApiModelTransformer[MainBusinessActivityView] { vs: VatScheme =>
      vs.vatSicAndCompliance.map(cc =>
        MainBusinessActivityView(cc.mainBusinessActivity.id,
          Some(SicCode(cc.mainBusinessActivity.id, cc.mainBusinessActivity.description, cc.mainBusinessActivity.displayDetails))))
    }

  }

}
