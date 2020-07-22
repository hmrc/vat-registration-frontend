/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import connectors.RegistrationConnector
import javax.inject.Inject
import models.CompanyProvideWorkers.PROVIDE_WORKERS_NO
import models.TemporaryContracts.TEMP_CONTRACTS_NO
import models._
import models.api.SicCode
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

  def getSicAndCompliance(implicit hc:HeaderCarrier, cp:CurrentProfile):Future[SicAndCompliance] = {
    s4lService.fetchAndGetNoAux[SicAndCompliance](SicAndCompliance.sicAndCompliance).flatMap{
      _.fold(getFromApi)(a => Future.successful(a))
    }
  }

  def updateSicAndCompliance[T](newData: T)(implicit hc:HeaderCarrier, cp:CurrentProfile): Future[SicAndCompliance] = {
    getSicAndCompliance.flatMap(sac => isModelComplete(updateModel(sac, newData)).fold(
      incomplete => s4lService.saveNoAux[SicAndCompliance](incomplete, SicAndCompliance.sicAndCompliance).map(_ => incomplete),
      complete => updateVatRegAndClearS4l(complete)
    ))
  }

  def submitSicCodes(sicCodes: List[SicCode])(implicit cp:CurrentProfile, hc:HeaderCarrier): Future[SicAndCompliance] = {
    getSicAndCompliance flatMap { sac =>

      val sacWithCodes = sac.copy(otherBusinessActivities = Some(OtherBusinessActivities(sicCodes)))

      val newSac = if (sicCodes.size == 1) {
        sacWithCodes.copy(
          mainBusinessActivity = Some(MainBusinessActivityView(sicCodes.head))
        )
      } else {
        sacWithCodes.copy(
          mainBusinessActivity = None
        )
      }

      val newView = if (!needComplianceQuestions(sicCodes)) {
        newSac.copy(companyProvideWorkers = None, workers = None, temporaryContracts = None, skilledWorkers = None)
      } else {
        newSac
      }
      updateSicAndCompliance(newView)
    }
  }

  private def getFromApi(implicit cp:CurrentProfile, hc:HeaderCarrier): Future[SicAndCompliance] = {
    for {
      optView <- registrationConnector.getSicAndCompliance
      view    =  optView.fold(SicAndCompliance())(SicAndCompliance.fromApi)
      _       <- s4lService.saveNoAux[SicAndCompliance](view, SicAndCompliance.sicAndCompliance)
    } yield view
  }

  private def updateModel[T](before: SicAndCompliance, newData: T): SicAndCompliance = {
    newData match {
      case a: BusinessActivityDescription => before.copy(description = Some(a))
      case b: MainBusinessActivityView    => before.copy(mainBusinessActivity = Some(b))
      case c: CompanyProvideWorkers       => before.copy(companyProvideWorkers = Some(c))
      case d: Workers                     => before.copy(workers = Some(d))
      case e: TemporaryContracts          => before.copy(temporaryContracts = Some(e))
      case f: SkilledWorkers              => before.copy(skilledWorkers = Some(f))
      case g: SicAndCompliance            => g
      case _                              => before
    }
  }

  // list of sics nil, 1 or many
  private def isModelComplete(view: SicAndCompliance): Completion[SicAndCompliance] = {
    view match {
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(OtherBusinessActivities(_)), Some(CompanyProvideWorkers(PROVIDE_WORKERS_NO)), _, _, _) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(OtherBusinessActivities(_)), Some(_), Some(Workers(nb)), _, _) if nb < 8 =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(OtherBusinessActivities(_)), Some(_), Some(_), Some(TemporaryContracts(TEMP_CONTRACTS_NO)), _) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(OtherBusinessActivities(_)), Some(_), Some(_), Some(_), Some(_)) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(OtherBusinessActivities(sicCodes)), None, None, None, None) if(!needComplianceQuestions(sicCodes)) =>
        Complete(view)
      case _ => Incomplete(view)
    }
  }

  private def updateVatRegAndClearS4l(completeModel: SicAndCompliance)(implicit hc:HeaderCarrier, cp:CurrentProfile): Future[SicAndCompliance] = {

    for {
      _ <- registrationConnector.updateSicAndCompliance(completeModel)
      _ <- s4lService.clear
    } yield completeModel
  }

  def needComplianceQuestions(sicCodes: List[SicCode]): Boolean = {
    val complianceSicCodes = List(
      "01610", "41201", "42110", "42910", "42990",
      "43120", "43999", "78200", "80100", "81210",
      "81221", "81222", "81223", "81291", "81299")
    lazy val isAllComplianceQuestions = sicCodes.map(_.code).forall(s => complianceSicCodes.contains(s))

    sicCodes.nonEmpty && isAllComplianceQuestions
  }
}
