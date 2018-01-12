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

package models

import common.ErrorUtil.fail
import models.api.VatEligibilityChoice._
import models.api._
import models.view.sicAndCompliance.labour.CompanyProvideWorkers.{PROVIDE_WORKERS_NO, PROVIDE_WORKERS_YES}
import models.view.sicAndCompliance.labour.SkilledWorkers.{SKILLED_WORKERS_YES, SKILLED_WORKERS_NO}
import models.view.sicAndCompliance.labour.TemporaryContracts.{TEMP_CONTRACTS_NO, TEMP_CONTRACTS_YES}
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.view.vatContact.BusinessContactDetails
import models.view.vatContact.ppob.PpobView
import models.view.vatTradingDetails.vatChoice.VoluntaryRegistration._
import models.view.vatTradingDetails.vatChoice.{OverThresholdView, TaxableTurnover, VoluntaryRegistration, VoluntaryRegistrationReason}
import play.api.libs.json._


trait S4LModelTransformer[C] {
  // Returns an S4L container for a logical group given a VatScheme
  def toS4LModel(vatScheme: VatScheme): C
}

trait S4LApiTransformer[C, API] {
  // Returns logical group API model given an S4L container
  def toApi(container: C): API
}

final case class S4LVatSicAndCompliance(
  description: Option[BusinessActivityDescription] = None,
  mainBusinessActivity: Option[MainBusinessActivityView] = None,

  //Labour Compliance
  companyProvideWorkers: Option[CompanyProvideWorkers] = None,
  workers: Option[Workers] = None,
  temporaryContracts: Option[TemporaryContracts] = None,
  skilledWorkers: Option[SkilledWorkers] = None)

object S4LVatSicAndCompliance {

  def fromApiReads(json: JsValue): S4LVatSicAndCompliance = {

    val sicCode = (json \ "mainBusinessActivity").as[SicCode]
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

    S4LVatSicAndCompliance(
      description = Some(BusinessActivityDescription((json \ "businessDescription").as[String])),
      mainBusinessActivity = Some(MainBusinessActivityView(id = sicCode.id, mainBusinessActivity = Some(sicCode))),
      companyProvideWorkers = numOfWorkers.map(n => CompanyProvideWorkers(if (n == 0) PROVIDE_WORKERS_NO else PROVIDE_WORKERS_YES)),
      workers = workers,
      temporaryContracts = temporaryContracts,
      skilledWorkers = skilledWorkers
    )
  }

  implicit val toApiWrites = new Writes[S4LVatSicAndCompliance] {
    override def writes(o: S4LVatSicAndCompliance): JsValue = {

      val provideWorkers = o.companyProvideWorkers.map(a => a.yesNo)

      val numOfWorkers =   o.workers.map{a =>
        if(provideWorkers.exists(b => b == PROVIDE_WORKERS_NO)) 0
        else a.numberOfWorkers
      }
      val temp = o.temporaryContracts.map{a =>
        if(provideWorkers.exists(b => b == PROVIDE_WORKERS_NO)) None
        else if(a.yesNo == TEMP_CONTRACTS_YES) Some(true)
        else Some(false)
      }
      val skill =  o.skilledWorkers.map { a =>
        if (provideWorkers.exists(b => b == PROVIDE_WORKERS_NO)) None
        else if (a.yesNo == SKILLED_WORKERS_YES) Some(true)
        else Some(false)
      }
      
      val businessDesc =  Json.obj("businessDescription" -> o.description.get.description).asOpt[JsObject]

      val numberOfWorkers = Json.obj("numberOfWorkers" -> numOfWorkers).asOpt[JsObject]

      val temporaryCont = Json.obj("temporaryContracts" -> temp).asOpt[JsObject]

      val skilledWork = Json.obj("skilledWorkers" -> skill).asOpt[JsObject]

      val labour = provideWorkers.map(_ => Seq(numberOfWorkers, temporaryCont,skilledWork).flatten.fold(Json.obj())((a,b) => a ++ b))

      val mainBus = Json.obj("mainBusinessActivity" ->
        Json.toJson(o.mainBusinessActivity.get.mainBusinessActivity.get)(SicCode.format)).asOpt[JsObject]

      Seq(businessDesc, labour,mainBus).flatten.fold[JsObject](Json.obj())((a,b) => a ++ b)
    }
  }
  // utilities
  def dropLabour(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    deleteLabour((container))

  // labour List(LabProvidesWorkersPath, LabWorkersPath, LabTempContractsPath, LabSkilledWorkersPath)
  def dropFromCompanyProvideWorkers(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(workers = None, temporaryContracts = None, skilledWorkers = None)

  def dropFromWorkers(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(temporaryContracts = None, skilledWorkers = None)

  def dropFromTemporaryContracts(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(skilledWorkers = None)



  private def deleteLabour(container: S4LVatSicAndCompliance): S4LVatSicAndCompliance =
    container.copy(
      companyProvideWorkers = None,
      workers = None,
      temporaryContracts = None,
      skilledWorkers = None
    )

  implicit val format: OFormat[S4LVatSicAndCompliance] = Json.format[S4LVatSicAndCompliance]

  def error = throw fail("VatSicAndCompliance")

}

final case class S4LVatContact
(
  businessContactDetails: Option[BusinessContactDetails] = None,
  ppob: Option[PpobView] = None
)

object S4LVatContact {
  implicit val format: OFormat[S4LVatContact] = Json.format[S4LVatContact]

  implicit val modelT = new S4LModelTransformer[S4LVatContact] {
    override def toS4LModel(vs: VatScheme): S4LVatContact =
      S4LVatContact(
        businessContactDetails = ApiModelTransformer[BusinessContactDetails].toViewModel(vs),
        ppob = ApiModelTransformer[PpobView].toViewModel(vs)
      )
  }

  def error = throw fail("VatContact")

  implicit val apiT = new S4LApiTransformer[S4LVatContact, VatContact] {
    override def toApi(c: S4LVatContact): VatContact =
      VatContact(
        digitalContact = VatDigitalContact(
                          email = c.businessContactDetails.map(_.email).getOrElse(error),
                          tel = c.businessContactDetails.flatMap(_.daytimePhone),
                          mobile = c.businessContactDetails.flatMap(_.mobile)),
        website = c.businessContactDetails.flatMap(_.website),
        ppob = c.ppob.flatMap(_.address).getOrElse(error)
      )
  }
}


case class S4LVatEligibilityChoice(taxableTurnover: Option[TaxableTurnover] = None,
                                   voluntaryRegistration: Option[VoluntaryRegistration] = None,
                                   voluntaryRegistrationReason: Option[VoluntaryRegistrationReason] = None,
                                   overThreshold: Option[OverThresholdView] = None)

object S4LVatEligibilityChoice {
  implicit val format: OFormat[S4LVatEligibilityChoice] = Json.format[S4LVatEligibilityChoice]
  implicit val tradingDetails: S4LKey[S4LVatEligibilityChoice] = S4LKey("VatChoice")

  implicit val modelT = new S4LModelTransformer[S4LVatEligibilityChoice] {
    // map VatScheme to VatTradingDetails
    override def toS4LModel(vs: VatScheme): S4LVatEligibilityChoice =
      S4LVatEligibilityChoice(
        taxableTurnover = ApiModelTransformer[TaxableTurnover].toViewModel(vs),
        voluntaryRegistration = ApiModelTransformer[VoluntaryRegistration].toViewModel(vs),
        overThreshold = ApiModelTransformer[OverThresholdView].toViewModel(vs),
        voluntaryRegistrationReason = ApiModelTransformer[VoluntaryRegistrationReason].toViewModel(vs)
      )
  }

  def error = throw fail("VatChoice")

  implicit val apiT = new S4LApiTransformer[S4LVatEligibilityChoice, VatEligibilityChoice] {
    // map S4LTradingDetails to VatTradingDetails
    override def toApi(c: S4LVatEligibilityChoice): VatEligibilityChoice =
      VatEligibilityChoice(
        necessity = c.voluntaryRegistration.map(vr =>
          if (vr.yesNo == REGISTER_YES) NECESSITY_VOLUNTARY else NECESSITY_OBLIGATORY).getOrElse(NECESSITY_OBLIGATORY),
        reason = c.voluntaryRegistrationReason.map(_.reason),
        vatThresholdPostIncorp = c.overThreshold.map(vtp =>
          VatThresholdPostIncorp(
            overThresholdSelection = vtp.selection,
            overThresholdDate = vtp.date
          )
        )
      )
  }
}

final case class S4LVatEligibility
(
  vatEligibility: Option[VatServiceEligibility] = None

)


object S4LVatEligibility {
  implicit val format: OFormat[S4LVatEligibility] = Json.format[S4LVatEligibility]

  implicit val modelT = new S4LModelTransformer[S4LVatEligibility] {
    override def toS4LModel(vs: VatScheme): S4LVatEligibility =
      S4LVatEligibility(vatEligibility = ApiModelTransformer[VatServiceEligibility].toViewModel(vs))
  }

  def error = throw fail("VatServiceEligibility")

  implicit val apiT = new S4LApiTransformer[S4LVatEligibility, VatServiceEligibility] {
    override def toApi(c: S4LVatEligibility): VatServiceEligibility = {
      c.vatEligibility.map(ve => VatServiceEligibility(
        haveNino = ve.haveNino,
        doingBusinessAbroad = ve.doingBusinessAbroad,
        doAnyApplyToYou = ve.doAnyApplyToYou,
        applyingForAnyOf = ve.applyingForAnyOf,
        applyingForVatExemption = ve.applyingForVatExemption,
        companyWillDoAnyOf = ve.companyWillDoAnyOf,
        vatEligibilityChoice = ve.vatEligibilityChoice
        )
      ).getOrElse(error)
    }
  }
}
