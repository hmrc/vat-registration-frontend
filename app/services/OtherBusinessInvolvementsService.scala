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

package services

import config.Logging
import connectors.RegistrationApiConnector
import models.{CurrentProfile, OtherBusinessInvolvement, S4LKey}
import services.OtherBusinessInvolvementsService._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OtherBusinessInvolvementsService @Inject()(val s4LService: S4LService,
                                                 registrationApiConnector: RegistrationApiConnector
                                                )(implicit ec: ExecutionContext) extends Logging {

  def getOtherBusinessInvolvement(index: Int)(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Option[OtherBusinessInvolvement]] = {
    implicit val s4lKey: S4LKey[OtherBusinessInvolvement] = OtherBusinessInvolvement.s4lKey(index)

    s4LService.fetchAndGet[OtherBusinessInvolvement].flatMap {
      case None | Some(OtherBusinessInvolvement(None, None, None, None, None, None)) =>
        registrationApiConnector.getSection[OtherBusinessInvolvement](profile.registrationId, Some(index))
      case otherBusinessInvolvement => Future.successful(otherBusinessInvolvement)
    }
  }

  def getOtherBusinessInvolvements(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[List[OtherBusinessInvolvement]] = {
    registrationApiConnector.getListSection[OtherBusinessInvolvement](profile.registrationId)
  }

  def getHighestValidIndex(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[Int] =
    getOtherBusinessInvolvements.map(_.length + 1)

  def updateOtherBusinessInvolvement[T](index: Int, data: T)(implicit profile: CurrentProfile, headerCarrier: HeaderCarrier): Future[OtherBusinessInvolvement] = {
    implicit val s4lKey: S4LKey[OtherBusinessInvolvement] = OtherBusinessInvolvement.s4lKey(index)

    for {
      otherBusinessInvolvement <- getOtherBusinessInvolvement(index)
      updatedOtherBusinessInvolvement = updateModel(otherBusinessInvolvement.getOrElse(OtherBusinessInvolvement()), data)
      isComplete = isModelComplete(updatedOtherBusinessInvolvement)
      model <- isComplete.fold(
        incompleteData => s4LService.save[OtherBusinessInvolvement](incompleteData).map(_ => incompleteData),
        completeData => for {
          _ <- s4LService.clearKey[OtherBusinessInvolvement]
          result <- registrationApiConnector.replaceSection(profile.registrationId, completeData, Some(index))
        } yield result
      )
    } yield model
  }

  private[services] def updateModel[T](otherBusinessInvolvement: OtherBusinessInvolvement, data: T): OtherBusinessInvolvement =
    data match {
      case BusinessNameAnswer(answer) => otherBusinessInvolvement.copy(businessName = Some(answer))
      case HasVrnAnswer(answer) => otherBusinessInvolvement.copy(hasVrn = Some(answer))
      case VrnAnswer(answer) => otherBusinessInvolvement.copy(vrn = Some(answer))
      case HasUtrAnswer(answer) => otherBusinessInvolvement.copy(hasUtr = Some(answer))
      case UtrAnswer(answer) => otherBusinessInvolvement.copy(utr = Some(answer))
      case StillTradingAnswer(answer) => otherBusinessInvolvement.copy(stillTrading = Some(answer))
    }

  private[services] def isModelComplete(otherBusinessInvolvement: OtherBusinessInvolvement): Completion[OtherBusinessInvolvement] =
    otherBusinessInvolvement match {
      case OtherBusinessInvolvement(Some(businessName), Some(true), Some(vrn), _, _, Some(activelyTrading)) =>
        Complete(otherBusinessInvolvement.copy(hasUtr = None, utr = None))
      case OtherBusinessInvolvement(Some(businessName), Some(false), _, _, _, Some(activelyTrading)) => //TODO Remove this case when utr pages are added
        Complete(otherBusinessInvolvement.copy(vrn = None))
      case OtherBusinessInvolvement(Some(businessName), Some(false), _, Some(true), Some(utr), Some(activelyTrading)) =>
        Complete(otherBusinessInvolvement.copy(vrn = None))
      case OtherBusinessInvolvement(Some(businessName), Some(false), _, Some(false), _, Some(activelyTrading)) =>
        Complete(otherBusinessInvolvement.copy(vrn = None, utr = None))
      case _ =>
        Incomplete(otherBusinessInvolvement)
    }
}

object OtherBusinessInvolvementsService {
  case class BusinessNameAnswer(answer: String)

  case class HasVrnAnswer(answer: Boolean)

  case class VrnAnswer(answer: String)

  case class HasUtrAnswer(answer: Boolean)

  case class UtrAnswer(answer: String)

  case class StillTradingAnswer(answer: Boolean)
}