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

package features.sicAndCompliance.services

import javax.inject.Inject

import connectors.RegistrationConnector
import models.S4LKey.sicAndCompliance
import models.api.SicCode
import models.view.sicAndCompliance.labour.{CompanyProvideWorkers, SkilledWorkers, TemporaryContracts, Workers}
import models.view.sicAndCompliance.labour.CompanyProvideWorkers.{PROVIDE_WORKERS_NO, PROVIDE_WORKERS_YES}
import models.view.sicAndCompliance.labour.TemporaryContracts.{TEMP_CONTRACTS_NO, TEMP_CONTRACTS_YES}
import models.view.sicAndCompliance.labour.SkilledWorkers.{SKILLED_WORKERS_NO, SKILLED_WORKERS_YES}
import models.view.sicAndCompliance.{BusinessActivityDescription, MainBusinessActivityView}
import models.{CurrentProfile, S4LFlatRateScheme, S4LKey, S4LVatSicAndCompliance}
import services.{FlatRateService, S4LService, VatRegistrationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class SicAndComplianceServiceImpl @Inject()(val s4lService: S4LService,
                                            val vrs: VatRegistrationService,
                                            val registrationConnector: RegistrationConnector) extends SicAndComplianceService

trait SicAndComplianceService {

  val s4lService: S4LService
  val vrs: VatRegistrationService
  val registrationConnector: RegistrationConnector

  type Completion[T] = Either[T, T]
  val Incomplete   = scala.util.Left
  val Complete     = scala.util.Right


  def getSicAndCompliance(implicit hc:HeaderCarrier, cp:CurrentProfile):Future[S4LVatSicAndCompliance] = {
    s4lService.fetchAndGetNoAux[S4LVatSicAndCompliance](sicAndCompliance).flatMap{
      _.fold(getFromApi)(a => Future.successful(a))
    }
  }

  def updateSicAndCompliance[T](newData: T)(implicit hc:HeaderCarrier, cp:CurrentProfile): Future[S4LVatSicAndCompliance] = {
    getSicAndCompliance.flatMap(sac => isModelComplete(updateModel(sac, newData)).fold(
      incomplete => s4lService.saveNoAux[S4LVatSicAndCompliance](incomplete,S4LKey.sicAndCompliance).map(_ => incomplete),
      complete => updateVatRegAndClearS4l(complete)
    ))
  }

  private def getFromApi(implicit cp:CurrentProfile, hc:HeaderCarrier): Future[S4LVatSicAndCompliance] = {
    for {
      optView <- registrationConnector.getSicAndCompliance
      view    =  optView.fold(S4LVatSicAndCompliance())(S4LVatSicAndCompliance.fromApiReads)
      _       <- s4lService.saveNoAux[S4LVatSicAndCompliance](view, sicAndCompliance)
    } yield view
  }

  private def updateModel[T](before: S4LVatSicAndCompliance, newData: T):S4LVatSicAndCompliance = {
    newData match {
      case a: BusinessActivityDescription => before.copy(description = Some(a))
      case b: MainBusinessActivityView    => before.copy(mainBusinessActivity = Some(b))
      case c: CompanyProvideWorkers       => before.copy(companyProvideWorkers = Some(c))
      case d: Workers                     => before.copy(workers = Some(d))
      case e: TemporaryContracts          => before.copy(temporaryContracts = Some(e))
      case f: SkilledWorkers              => before.copy(skilledWorkers = Some(f))
      case _                              => before
    }
  }

  private def isModelComplete(view: S4LVatSicAndCompliance): Completion[S4LVatSicAndCompliance] = view match {
    case S4LVatSicAndCompliance(Some(_),Some(MainBusinessActivityView(_,Some(_))),Some(CompanyProvideWorkers(PROVIDE_WORKERS_NO)),_,_,_) =>
      Complete(view)
    case S4LVatSicAndCompliance(Some(_),Some(MainBusinessActivityView(_,Some(_))),Some(_),Some(Workers(nb)),_,_) if nb < 8 =>
      Complete(view)
    case S4LVatSicAndCompliance(Some(_),Some(MainBusinessActivityView(_,Some(_))),Some(_), Some(_), Some(TemporaryContracts(TEMP_CONTRACTS_NO)),_) =>
      Complete(view)
    case S4LVatSicAndCompliance(Some(_),Some(MainBusinessActivityView(_,Some(_))),Some(_),Some(_),Some(_),Some(_)) =>
      Complete(view)
    case S4LVatSicAndCompliance(Some(_),Some(MainBusinessActivityView(_,Some(_))),None,None,None,None) =>
      Complete(view)
    case _ => Incomplete(view)
  }

  private def updateVatRegAndClearS4l(completeModel: S4LVatSicAndCompliance)(implicit hc:HeaderCarrier, cp:CurrentProfile): Future[S4LVatSicAndCompliance] = {
    for {
      _ <- registrationConnector.updateSicAndCompliance(completeModel)
      _ <- s4lService.clear
    } yield completeModel
  }

  def dropLabour(implicit cp:CurrentProfile,hc:HeaderCarrier) = {
    getSicAndCompliance.map{sic =>
      val updated = S4LVatSicAndCompliance.dropLabour(sic)
      s4lService.saveNoAux[S4LVatSicAndCompliance](updated, sicAndCompliance)
    }
  }

  def saveMainBusinessActivity(sicCode: SicCode)(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[S4LVatSicAndCompliance] = {
    for {
      mainSic          <- getSicAndCompliance.map(_.mainBusinessActivity)
      selectionChanged = mainSic.exists(_.id != sicCode.id)
      viewModel        <- updateSicAndCompliance(MainBusinessActivityView(sicCode))
    } yield {
      if (selectionChanged) s4lService.save(S4LFlatRateScheme()).flatMap(_ => vrs.submitVatFlatRateScheme())

      viewModel
    }
  }

  def needComplianceQuestions(sicCodes: List[SicCode]): Boolean = {
    val complianceSicCodes = List(
      "01610", "41201", "42110", "42910", "42990",
      "43120", "43999", "78200", "80100", "81210",
      "81221", "81222", "81223", "81291", "81299")
    lazy val isAllComplianceQuestions = sicCodes.map(_.id).forall(s => s.length == 8 && complianceSicCodes.contains(s.substring(0, 5)))

    sicCodes.nonEmpty && isAllComplianceQuestions
  }
}
