/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.VatRegistrationConnector
import javax.inject.{Inject, Singleton}
import models._
import models.api.SicCode
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SicAndComplianceService @Inject()(val s4lService: S4LService,
                                        val vrs: VatRegistrationService,
                                        val registrationConnector: VatRegistrationConnector)
                                       (implicit ec: ExecutionContext) {

  def getSicAndCompliance(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[SicAndCompliance] = {
    s4lService.fetchAndGetNoAux[SicAndCompliance](SicAndCompliance.s4lKey).flatMap {
      _.fold(getFromApi)(a => Future.successful(a))
    }
  }

  def updateSicAndCompliance[T](newData: T)(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[SicAndCompliance] = {
    getSicAndCompliance.flatMap(sac => isModelComplete(updateModel(sac, newData)).fold(
      incomplete => s4lService.saveNoAux[SicAndCompliance](incomplete, SicAndCompliance.s4lKey).map(_ => incomplete),
      complete => updateVatRegAndClearS4l(complete)
    ))
  }

  def submitSicCodes(sicCodes: List[SicCode])(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[SicAndCompliance] = {
    getSicAndCompliance flatMap { sac =>
      val sacWithCodes = sac.copy(businessActivities = Some(BusinessActivities(sicCodes)))

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
        newSac.copy(supplyWorkers = None, workers = None, intermediarySupply = None)
      } else {
        newSac
      }

      updateSicAndCompliance(newView)
    }
  }

  private def getFromApi(implicit cp: CurrentProfile, hc: HeaderCarrier): Future[SicAndCompliance] = {
    for {
      optView <- registrationConnector.getSicAndCompliance
      view = optView.fold(SicAndCompliance())(SicAndCompliance.fromApi)
      _ <- s4lService.saveNoAux[SicAndCompliance](view, SicAndCompliance.s4lKey)
    } yield view
  }

  private def updateModel[T](before: SicAndCompliance, newData: T): SicAndCompliance = {
    newData match {
      case a: BusinessActivityDescription => before.copy(description = Some(a))
      case b: MainBusinessActivityView => before.copy(mainBusinessActivity = Some(b))
      case c: SupplyWorkers => {
        if (c.yesNo) {
          before.copy(supplyWorkers = Some(c), intermediarySupply = None)
        }
        else {
          before.copy(supplyWorkers = Some(c), workers = None)
        }
      }
      case d: Workers => before.copy(workers = Some(d))
      case e: IntermediarySupply => before.copy(intermediarySupply = Some(e))
      case g: SicAndCompliance => g
      case _ => before
    }
  }

  // list of sics nil, 1 or many
  private def isModelComplete(view: SicAndCompliance): Completion[SicAndCompliance] = {
    view match {
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(BusinessActivities(_)), Some(SupplyWorkers(false)), _, Some(_)) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(BusinessActivities(_)), Some(SupplyWorkers(true)), Some(Workers(_)), _) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(BusinessActivities(_)), Some(_), Some(_), Some(IntermediarySupply(false))) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(BusinessActivities(_)), Some(_), Some(_), Some(_)) =>
        Complete(view)
      case SicAndCompliance(Some(_), Some(MainBusinessActivityView(_, Some(_))), Some(BusinessActivities(sicCodes)), None, None, None) if !needComplianceQuestions(sicCodes) =>
        Complete(view)
      case _ => Incomplete(view)
    }
  }

  private def updateVatRegAndClearS4l(completeModel: SicAndCompliance)(implicit hc: HeaderCarrier, cp: CurrentProfile): Future[SicAndCompliance] = {
    for {
      _ <- registrationConnector.updateSicAndCompliance(completeModel)
      _ <- s4lService.clear
    } yield completeModel
  }


  def needComplianceQuestions(sicCodes: List[SicCode]): Boolean = {
    val complianceSicCodes = Set(
      "42110", "42910", "43999", "41201", "43120", "42990",
      "01610", "78200", "80100", "81210", "81221", "81222",
      "81223", "81291", "81299")

    complianceSicCodes.intersect(sicCodes.map(_.code).toSet).nonEmpty
  }
}
